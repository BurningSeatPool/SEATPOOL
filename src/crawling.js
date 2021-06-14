const webdriver = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const chromedriver = require('chromedriver');
const mysql = require('mysql');


const connection = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '112233',
    database: 'testfile'
});

const options = new chrome.Options();

options.addArguments('--disable-dev-shm-usage')
options.addArguments('--no-sandbox')
options.addArguments('--single-process')
options.addArguments('--headless')
options.addArguments('--disable-gpu')
options.addArguments('--disable-extensions')

//const driver = new webdriver.Builder().forBrowser('chrome').setChromeOptions(new chrome.Options().addArguments('headless')).build(); // 이렇게하면 chrome 창 안띄움
const driver = new webdriver.Builder().forBrowser('chrome').setChromeOptions(options).build();
//const driver = new webdriver.Builder().forBrowser('chrome').build(); 이렇게 하면 chrome 창 띄움
const base_url = "http://www.letskorail.com/ebizprd/EbizPrdTicketPr21100W_pr21150.do";



function ktx_info(month, day, time, from, to, callback){
    try{
        const run = async () => {
            await driver.get(base_url);
            const By = webdriver.By;
    
            const dep = await (await driver).findElement(By.css("input#start.inp250"));
            (await driver).sleep(10);
            await dep.clear();
            (await driver).sleep(10);
            await dep.sendKeys(from);
            
            const arriv = await (await driver).findElement(By.xpath("/html/body/div[1]/div[3]/div/div[1]/div[2]/form/div/div[2]/div/dl[4]/dd/input[1]"));
            (await driver).sleep(10);
            await arriv.clear();
            (await driver).sleep(10);
            await arriv.sendKeys(to);
    
            await (await driver).findElement(By.xpath("/html/body/div[1]/div[3]/div/div[1]/div[2]/form/div/div[2]/div/dl[5]/dd/select[2]")).sendKeys(parseInt(month));
    
            await (await driver).findElement(By.id("s_day")).sendKeys(parseInt(day));
    
            await (await driver).findElement(By.id("s_hour")).sendKeys(time);
    
            await (await (await driver).findElement(By.css("img[alt*=조회하기]"))).click();
            
            await (await driver).switchTo().frame((await driver).findElement(By.tagName("iframe")));
    
            await (await (await driver).findElement(By.className("btn_blue_ang"))).click();
    
            (await driver).sleep(10);
    
            (await driver).switchTo().parentFrame();
    
            await delay(0.6); //4.1초 정도걸림
            // await delay(0.5) 로 하면 4.0초까지 단축
    
            const list = await (await driver).findElements(By.xpath("//table/tbody/tr"));
            console.log("total : " + list.length + " rows");
            process.stdout.write("Loaded from web");
            
            for(var i=1;i<list.length;i++){
                const path = "/html/body/div[1]/div[3]/div/div/form[1]/div[1]/div[4]/table[1]/tbody/tr[";
                const sale = "]/td[5]/a[1]/img";
                const goStyle = "]/td[1]";
                const trNo = "]/td[2]/a/span";
                const startInfo = "]/td[3]";
                const endInfo = "]/td[4]";
                //const spendTime = "]/td[9]";
    
                var tr_no;
                var tr_start;
                var tr_end;
                var tr_fair;
                var tr_sale;
    
                const trainStyle = (await driver).findElement(By.xpath(path+i+goStyle)).then(function (trainStyle){
                    trainStyle.getText().then(function (trainStyle){
                        //console.log("Train Style : ",trainStyle);
                    })
                })
                const trainNum = (await driver).findElement(By.xpath(path+i+trNo)).then(function (trainNum){
                    trainNum.getText().then(function (trainNum){
                        tr_no = trainNum;
                        //console.log("Train No : ",trainNum)
                    })
                })
    
                var start = await(await driver).findElement(By.xpath(path+i+startInfo)).then(function (start){
                    start.getText().then(function (start){
                        var star = start.split('\n');
                        var startTime = star[1];
                        tr_start = startTime;
                    })
                })
    
                var end = (await driver).findElement(By.xpath(path+i+endInfo)).then(function (end){
                    end.getText().then(function (end){
                        var en = end.split('\n')
                        var endTime = en[1];
                        tr_end = endTime;
                    })
                })
                
                const trainSale = (await driver).findElement(By.xpath(path+i+sale)).then(function (trainSale){
                    trainSale.getAttribute('alt').then(function (alt){
                        const rate = alt.substring(0, 2);
                        tr_sale = rate+'%할인';
                        tr_fair = price(from, to, function (result) {
                            delay(0.2);
                            var timecode = TableConvert(tr_no, from, to, month, day);

                            connection.query(`insert ignore into train_info (Train_ID, Depart, Dest, month, day, Depart_Time, Arrival_Time, Amount, timecode, sale) values (
                                '${tr_no}','${from}','${to}', '${month}', '${day}','${tr_start}','${tr_end}','${round(result * (100 - rate) / 100)}','${timecode}','${tr_sale}')`, function (error, results, fields) {
                                if (error) {
                                    console.log(error);
                                }
                            });
                        });
                    })
                });
                process.stdout.write(".");
            }
            callback("DONE");       
            (await driver).sleep(10);
        }
        run();
        
    }catch(e){
        console.log("Error");
    }
}



function delay(n){
    return new Promise(function(resolve){
        setTimeout(resolve,n*1000);
    });
};


function getTimeText(time){
    if(time<10){
        time = time+" (오전"+time+")";
    }else if(time>=10 && time <12){
        time = time+" (오전"+time+")";
    }else if(time>=12 && time <22){
        time = time+" (오후0"+(time-12)+")";
    }else{
        time = time+" (오후"+(time-12)+")";
    }
    return time;
}

function round(num){
    const ten = num%100;
    if(ten>50){
        return num+(100-ten);
    }else{
        return num-ten;
    }
}

function price(st, end, callback){
    const sqlQuery = `select fair from train_fair where st='${st}' and end='${end}'`;
    connection.query(sqlQuery, function (error, results, fields) {
        if (error) {
            console.log(error);
        }else{
            let price = parseInt(results[0].fair);
            return callback(price);
        }
    });  
}



function TableConvert(train_id, Depart, Dest, month, day){
    var ownCode;    
    ownCode = train_id+Depart+Dest+month+day
    return ownCode;
}


module.exports = ktx_info;