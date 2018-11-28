const functions = require('firebase-functions');
const admin = require('firebase-admin');
exports.handler = (change,context) => {



  if(!change.after.val()){
     console.log('A notification has been deleted','deleted');
     return false;
  }

const list=change.after.val();
 var payload={
    data:{
      title:"New Notification Added",
      body:`New Notification from CR`,
      icon:"default",
      click_action:"com.example.abdul.pucitstudentportalsystem_TARGET_POST",
      type:"notification"
    }
  };

  return admin.messaging().sendToDevice(list,payload).then(response=>{
     return console.log('this is notification work');
     });

  /*
   * You can store values as variables from the 'database.ref'
   * Just like here, I've done for 'user_id' and 'notification'
   */

  
  };