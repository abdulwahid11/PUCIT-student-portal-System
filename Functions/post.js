const functions = require('firebase-functions');
const admin = require('firebase-admin');
exports.handler = (change,context) => {


  if(!change.after.val()){
     console.log('A postNotification has been deleted','deleted');
     return false;
  }
const batch=context.params.batch_id;

const tokens=admin.database().ref(`/${batch}/postNotificatoins`).once('value');

return tokens.then(fromUserResult=>{
  var deviceTokens=fromUserResult.val().deviceTokens;
  var type=fromUserResult.val().type;

//const list=change.after.val();
 var payload={
    data:{
      title:"New Post Added",
      body:`CR posted in the group`,
      icon:"default",
      click_action:"com.example.abdul.pucitstudentportalsystem_TARGET_POST",
      type:type
    }
  };
  if(type==="notification"){
    payload.data.title="New Notification";
    payload.data.body="CR added to notifications";
    payload.data.type="notification";
  }

  return admin.messaging().sendToDevice(deviceTokens,payload).then(response=>{
     return console.log('this is post work');
     });

  /*
   * You can store values as variables from the 'database.ref'
   * Just like here, I've done for 'user_id' and 'notification'
   */



});
};