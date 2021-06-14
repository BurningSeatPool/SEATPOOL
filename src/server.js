const mysql = require('mysql');
const express = require("express");
const firebase = require('firebase');
var schedule = require('node-schedule');
const request = require('request');

const ktx_info = require('./crawling');

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
    apiKey: APIKEY,
    authDomain: AUTHDOMAIN,
    databaseURL: DATABASE_URL,
    projectId: PORJECT_ID,
    storageBucket: STORAGE_BUCKET,
    messagingSenderId: MESSAGING_SENDER_ID,
    appId: APP_ID,
    measurementId: MEASUREMENT
  };

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
  
const database = firebase.database();
  
app.post("/group/start", (req, res) => {   // parameter : roomName, roomid, price, time_info   http://localhost:3000/roomname="0525서울부산..."&roomid="MXWci34.."&price=49600&month=5&day=28
    req = req.query;
    console.log("Group created!");

    var lastjob;
  
    roomName = req.roomname;      //Room name
    roomid = req.roomid;          //Room id
    price = (req.price)*4;        //Ticket price - 4 mans
    month =6// req.month;            //month
    day = 14//req.day;                //day
    console.log(`roomName: ${roomName} ,roomid: ${roomid} ,price: ${price} ,month: ${month} ,day: ${day}`);
    
    //Before 4 days
    var beforeFour = day_mover(month, day, 0);
	//schedule.scheduleJob(`10 57 15 ${day} ${month} ?`, function(){		for test
    schedule.scheduleJob(`10 10 2 ${beforeFour[1]} ${beforeFour[0]} ?`, function(){
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
    var beforeThree = day_mover(month, day, 0);
	//schedule.scheduleJob(`20 57 15 ${day} ${month} ?`, function(){		for test
    schedule.scheduleJob(`25 10 2 ${beforeThree[1]} ${beforeThree[0]} ?`, function(){
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
    var beforeTwo = day_mover(month, day, 0);
	//schedule.scheduleJob(`30 57 15 ${day} ${month} ?`, function(){		for test
    schedule.scheduleJob(`40 10 2 ${beforeTwo[1]} ${beforeTwo[0]} ?`, function(){
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
                if(notPaidNum != 0 || userNum != 4){
                    console.log("돈 아직 안낸사람 수 : ",notPaidNum);
                    console.log("멤버 수 : ",userNum);
                    for(uid in chatgroup){
                        User.child(uid).child('myChatRoom').set('0');
                        User.child(uid).get().then( (snapshot) => {
                            var userinfo = snapshot.val();
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
                    console.log(`Room id : ${roomid} deleted`);
                }
            }, 3000);
        });
    });

    //D day
    //roomName : 0616서울05:15부산07:49chatrooms
    // Hour : roomName.substring(6,8);
    // minute : roomName.substring(9,11);
    var hour = roomName.substring(6,8);
    var minute = roomName.substring(9,11);
    var shiftTime = timeAdd(hour, minute, 20);
   // schedule.scheduleJob(`0 ${shiftTime[1]} ${shiftTime[0]} ${day} ${month} ?`, function(){
    schedule.scheduleJob(`10 39 1 ${day} ${month} ?`, function(){
        console.log(`Room id : ${roomid} D-day`);
        var roomInfo = database.ref(`${roomName}`).child(`${roomid}`);

        roomInfo.child('noAppeared').get().then( (snapshot) => {
            var noAppearedVal = snapshot.val();

            if(noAppearedVal > 0){
                var User = database.ref('Users');
                roomInfo.child('testUsers').get().then((snapshot) => {
                    var userid = snapshot.val();
                    roomInfo.child('host').get().then((snapshot)=> {
                        var host = snapshot.val();
                        for(uid in userid){
                            if(uid != host){
                                User.child(uid).get().then((snapshot)=> {
                                    VerifyPay(snapshot.val().merchant_uid, function(token, UID){
                                        Refund(token, UID, snapshot.val().uid);
                                        console.log(`>>${UID} Refund end.`);
                                    });
                                })
                            }
                            User.child(uid).get().then((snapshot)=> {
                                pushMsg(snapshot.val().pushToken, "출발시간에 방장이 나타나지 않았습니다.", "방장을 제외한 인원의 요금은 환불됩니다");        
                            });
                            User.child(uid).child('myChatRoom').set('0');
                        }
                    })
                })
               roomInfo.remove();
               lastjob.cancel();
            } 
        });
    });

    //After 1 day
    var afterOne = day_mover(month, day, 1);
	//lastjob = schedule.scheduleJob(`40 57 15 ${day} ${month} ?`, function(){		for test
    lastjob = schedule.scheduleJob(`0 0 8 ${afterOne[1]} ${afterOne[0]} ?`, function(){
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

function timeAdd(hour, minute, addVal){
    if((minute + addVal) >= 60){
        minute = minute+addVal-60;
        hour++;
    }
    return [hour, minute]
}

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
            'Authorization' : AUTHORIZATION,
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
	    if(msg == 'TRUE'){
	            console.log(`>>${UID} Verification end.`);
	            database.ref('Users').child(fbUser).child('paid').set('1');
	    }
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
    imp_key: IMP_KEY,
    imp_secret: IMP_SECRET
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
