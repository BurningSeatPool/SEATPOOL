const webdriver = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const chromedriver = require('chromedriver');
const mysql = require('mysql');
const express = require("express");
const firebase = require('firebase');
var schedule = require('node-schedule');
const request = require('request');

var app = express();

const port = 3000;

const connection = mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: '112233',
    database: 'testfile'
});

app.listen(port, () => {
    console.log(`Server started from port ${port}`);
    connection.connect( () => {
        console.log("Database connected");
    });
});

app.get("/", (req,res) => {
    res.send("Server Connected");
})

//"5", "19", getTimeText(04), "서울", "부산"

app.get("/ktx/search", (req, res) => {
    req=req.query;
    ktx_info(req.month, req.day, req.Depart_Time, req.Depart, req.Dest, function(msg){
        res.send(msg);
        console.log(msg);
    });
})

app.get("/ktx/get/timetable", (req, res) => {
    req=req.query;
    const sqlQuery = `SELECT Train_ID, Depart, Dest, Depart_Time, Arrival_Time, Amount, timecode, sale FROM train_info WHERE Depart_Time >= '${req.Depart_Time}:00' and month='${req.month}' and day='${req.day}' and Depart='${req.Depart}' and Dest='${req.Dest}' ORDER BY Depart_Time ASC;`;
    connection.query(sqlQuery, function (error, results, fields) {
        if (error) {
            console.log(error);
        }
        console.log(sqlQuery);
        res.json(results);
    });  
});

app.get("/ktx/get/count", (request, response) => {
    const req = request.query;
    const sqlQuery = `SELECT COUNT (*) AS cnt FROM train_info WHERE Depart_Time >= '${req.Depart_Time}:00' and month='${req.month}' and day='${req.day}' and Depart='${req.Depart}' and Dest='${req.Dest}';`;
    connection.query(sqlQuery, function (error, results, fields) {
        if (error) {
            console.log(error);
        }
        response.json(results[0].cnt);
    });  
});

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
                        tr_fair = price(from, to, function(result){                            
                            delay(0.2);
                            var timecode = TableConvert(tr_no, from, to, month, day);
                            
                            connection.query(`insert ignore into train_info (Train_ID, Depart, Dest, month, day, Depart_Time, Arrival_Time, Amount, timecode, sale) values (
                                '${tr_no}','${from}','${to}', '${month}', '${day}','${tr_start}','${tr_end}','${round(result*(100-rate)/100)}','${timecode}','${tr_sale}')`, function (error, results, fields) {
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

function TableConvert(train_id, Depart, Dest, month, day){
    var ownCode;    
    ownCode = train_id+Depart+Dest+month+day
    return ownCode;
}

//Reset train info data every 4 AM
schedule.scheduleJob('0 0 4 * * ?', function(){
    const sqlQuery = "delete from train_info";

    connection.query(sqlQuery, function (error, results, fields) {
        if (error) {
            console.log(error);
        }
        console.log(sqlQuery);
        console.log(results);
        console.log("Data reset");
    });  
});

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
var firebaseConfig = {
    apiKey: "API_KEY",
    authDomain: "DOMAIN",
    databaseURL: "DATA_BASE_URL",
    projectId: "PROJECT_ID",
    storageBucket: "STORAGE_BUCKET",
    messagingSenderId: "MESSAGE_SENDER_ID",
    appId: "APP_ID",
    measurementId: "MEASUREMENT_ID"
};
  
// Initialize Firebase
//firebase.initializeApp(firebaseConfig);
  
//const database = firebase.database();
  
app.post("/group/start", (req, res) => {   // parameter : roomid, price, time_info   http://localhost:3000/roomname="0525서울부산..."&roomid="MXWci34.."&price=49600&month=5&day=28
    req = req.query;
    console.log("Group created!");

    var lastjob;
  
    roomName = req.roomname;      //Room name
    roomid = req.roomid;          //Room id
    price = (req.price)*4;        //Ticket price - 4 mans
    month =6// req.month;            //month
    day = 2//req.day;                //day
    console.log(`roomName: ${roomName} ,roomid: ${roomid} ,price: ${price} ,month: ${month} ,day: ${day}`);
    
    //Before 4 days
    var beforeFour = day_mover(month, day, -4);
	//schedule.scheduleJob(`10 57 15 ${day} ${month} ?`, function(){		for test
    schedule.scheduleJob(`0 0 8 ${beforeFour[1]} ${beforeFour[1]} ?`, function(){
        console.log(`Room id : ${roomid} Before 4 days`);
        database.ref(`${roomName}`).child(`${roomid}`).child('testUsers').get().then( (snapshot) => {
            var chatgroup = snapshot.val();
            var User = database.ref('Users');

            for(uid in chatgroup){
                User.child(uid).get().then( (snapshot) => {
                    var userinfo = snapshot.val();
                    if(userinfo.paid == '0'){
						pushMsg(userinfo.pushToken, "아직 결제를 하지 않으셨습니다.", "어서 결제해 주세요!");
                    }
                });
            }
        });
    });

    //Before 3 days
    //var beforeThree = day_mover(month, day, -3);
	//schedule.scheduleJob(`20 57 15 ${day} ${month} ?`, function(){		for test
    schedule.scheduleJob(`0 0 8 ${beforeThree[1]} ${beforeThree[1]} ?`, function(){
        console.log(`Room id : ${roomid} Before 3 days`);
        database.ref(`${roomName}`).child(`${roomid}`).child('testUsers').get().then( (snapshot) => {
            var chatgroup = snapshot.val();
            var User = database.ref('Users');
            var notPaidNum = 0;
            var userNum = 0;

            for(uid in chatgroup){
                User.child(uid).get().then( (snapshot) => {
                    var userinfo = snapshot.val();
                    userNum++;
                    if(userinfo.paid == '0'){
                        notPaidNum++;
                    }
                })
            }
            setTimeout(()=>{
                if(notPaidNum != 0 || userNum != 4){
                    console.log("member count :",userNum);
                    for(uid in chatgroup){
                        User.child(uid).get().then( (snapshot) => {
                            var userinfo = snapshot.val();
							pushMsg(userinfo.pushToken, "아직 결제를 하지 않으신 분이 있습니다..", "오늘까지 결제하지 않으시면 내일 방이 사라집니다.");
                        });
                    }
                }
            }, 3000);
        });
    });

    //Before 2 days
    //var beforeTwo = day_mover(month, day, -2);
	//schedule.scheduleJob(`30 57 15 ${day} ${month} ?`, function(){		for test
    schedule.scheduleJob(`0 0 8 ${beforeTwo[1]} ${beforeTwo[1]} ?`, function(){
        console.log(`Room id : ${roomid} Before 2 days`);
        database.ref(`${roomName}`).child(`${roomid}`).child('testUsers').get().then( (snapshot) => {
            var chatgroup = snapshot.val();
            var User = database.ref('Users');
            var notPaidNum = 0;
            var userNum = 0;
            
            for(uid in chatgroup){
                User.child(uid).get().then( (snapshot) => {
                    var userinfo = snapshot.val();
                    userNum++;
                    if(userinfo.paid == '0'){
                        notPaidNum++;
                    }
                })
            }setTimeout(()=>{
                if(notPaidNum != 0 || userNum != 1){
                    console.log("돈 아직 안낸사람 수 : ",notPaidNum);
                    console.log("멤버 수 : ",userNum);
                    for(uid in chatgroup){
                        User.child(uid).get().then( (snapshot) => {
                            var userinfo = snapshot.val();
                            User.child(uid).child('myChatRoom').set('0');
                            if(userinfo.paid == '1'){
                                VerifyPay(userinfo.merchant_uid, function(token, UID){
                                    Refund(token, UID, userinfo.uid);
                                    console.log(`>>${UID} Refund end.`);
                                });
                            }
                        });
                    }
                    //room delete function
                    database.ref(`${roomName}`).child(`${roomid}`).remove();
                    lastjob.cancel();
                    console.log(`Room id : ${roomid}deleted`);
                }
            }, 3000);
        });
    });

    //After 1 day
    //var afterOne = day_mover(month, day, 1);
	//lastjob = schedule.scheduleJob(`40 57 15 ${day} ${month} ?`, function(){		for test
    schedule.scheduleJob(`0 0 8 ${afterOne[1]} ${afterOne[1]} ?`, function(){
        console.log(`Room id : ${roomid} After 1 day`);
        var roomInfo = database.ref(`${roomName}`).child(`${roomid}`);
        roomInfo.child('testUsers').get().then( (snapshot) => {
            var chatgroup = snapshot.val();
            var User = database.ref('Users');
            
            for(uid in chatgroup){
                User.child(uid).child('myChatRoom').set('0');
            }
            roomInfo.remove();
        });
        roomInfo.child('host').get().then((snapshot) => {
            var host = snapshot.val();
            database.ref('Users').child(host).get().then((snapshot) => {
                var currentUser = snapshot.val();
                currentUser.child('money').set(price);
				pushMsg(currentUser.pushToken, "요금이 환급되었습니다", "마이페이지에서 확인하세요");
	            console.log(`host : ${host} <- money : ${price}`);
            });
        })
    });
    res.send("BOOKED");
});

function day_mover(month, day, shift){
    day=day+shift;
    if(day<=0){
      if(month == 1||month == 3||month == 5||month == 7||month == 8||month == 10||month == 12){
        day=day+31;
        month--;
      }else{
        day=day+30;
        month--;
      }
    }
    if(month == 1||month == 3||month == 5||month == 7||month == 8||month == 10||month == 12){
      if(day>31){
        day=day-31;
        month++;
      }
    }
    if(month == 2||month == 4||month == 6||month == 9||month == 11){
      if(day>30){
        day=day-30;
        month++;
      }
    }
    return [month, day]; 
}
 
function pushMsg(token, title, message){
	const user = {
        "to":token, //Push Token
        "priority" : "high",
        "data" : {
          "title" : title,
          "message" : message
        }
    };
    request.post({
        url: 'https://fcm.googleapis.com/fcm/send',
        body: JSON.stringify(user),
        headers: {
            'Authorization' : 'key=Authorization data',
            'Content-Type' : 'application/json'
        }
    }, function(error, response, body){
        console.log(body);
        console.log(" Push Message Sent to Member");
    })
}

app.post("/payment/verify", (req,res) => {    //localhost:3000/payment/verify?muid="merchant_id"&fbid="Firebase current user id"
    req=req.query;
    UID = req.muid;
    fbUser = req.fbid;
    VerifyPay(UID, function(token, UID){
        SearchList(token, UID, function(msg){
            res.send(msg);
            console.log(`>>${UID} Verification end.`);
            database.ref('Users').child(fbUser).child('paid').set('1');
        });
    })
});
  
app.post("/payment/refund", (req,res) => {       //localhost:3000/payment/refund?muid="merchant_id"&fbid="Firebase current user id"
    req=req.query;
    UID = req.muid;
    fbUser = req.fbid;
    console.log("refund");
    
    VerifyPay(UID, function(token, UID){
        Refund(token, UID, fbUser)
        console.log(`>>${UID} Refund end.`);
    });
});

function Refund(token, uid, fbUser){
    request.post({
        url: 'https://api.iamport.kr/payments/cancel?_token='+token,
        formData: {
            merchant_uid : uid
        }
    }, function(error, response, body){
        if(JSON.parse(body)['response'] != null){
            console.log("Refund amount : ",JSON.parse(body)['response']['amount']);
            database.ref('Users').child(fbUser).child('paid').set('0');
	        database.ref('Users').child(fbUser).child('merchant_uid').set('0');
        }else{
	        console.log("ERROR");
        }
    });
}

const iamport = {
    imp_key: 'IMP_KEY',
    imp_secret: 'IMP_SECRET'
};
 
function VerifyPay(uid, callback){
    request.post({
        url: 'https://api.iamport.kr/users/getToken',
        formData: iamport
    }, function(error, response, body){
        callback(token = JSON.parse(body)['response']['access_token'], uid);
    });
}
  
function SearchList(token, uid, callback){
    request.get({
        url: 'https://api.iamport.kr/payments/status/paid?limit=20&sorting=-started&_token='+token //+'&from='+(new Date()/1000-300)  //최근 5분이내에 결제한 사람들 필터링
    }, function(error, response, body){
        const result = JSON.parse(body)['response'];
        if(result != null){
            for(var i=0;i<result['list'].length;i++){
                if(uid == result['list'][i]['merchant_uid']){
                    console.log('>>TRUE. ',result['list'][i]['merchant_uid'],' PAID');
                    callback("TRUE");
                    return;
                }
            }
            console.log(`>>FALSE. ${uid} DID NOT PAID`);
            callback("FALSE");
        }
    });
}  
