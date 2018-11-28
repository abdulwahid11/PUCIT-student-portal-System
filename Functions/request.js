const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);
exports.handler = (change, context) => {


  /*
   * You can store values as variables from the 'database.ref'
   * Just like here, I've done for 'user_id' and 'notification'
   */

  const user_id = context.params.reciever_id;
  const sender = context.params.sender_id;
  const batch=context.params.batch_id;


  console.log('User ID', context.params.user_id);


  if(!change.after.val()){
  	return console.log('A notification has been deleted',"unknown");
  }

  const fromUser=admin.database().ref(`/${batch}/RequestNotifications/${user_id}/${sender}`).once('value');
  return fromUser.then(fromUserResult=>{

  	const requestType=fromUserResult.val().type;


  	 console.log('New notification from ',sender);

  	 const userQuery=admin.database().ref(`/${batch}/users/${sender}/userName`).once('value');
  	const deviceToken=admin.database().ref(`/${batch}/users/${user_id}/deviceToken`).once('value');
  

return Promise.all([userQuery,deviceToken]).then(result=>{
	const userName=result[0].val();
	const token_id=result[1].val();
	//var payload="hello";
  var payload={
    data:{
      title:"New Friend Request",
      body:`${userName} sends you a new friend request`,
      icon:"default",
      click_action:"com.example.abdul.pucitstudentportalsystem_TARGET_NOTIFICATION",
      from_user_id:sender,
      type:requestType,
      name:userName
    }
  };

	if(requestType==="message"){
    payload.data.title="New message recieved";
    payload.data.body=`${userName} sends you a text Message`;
    payload.data.click_action="com.example.abdul.pucitstudentportalsystem_TARGET_MESSAGE";
		
	}
  else if (requestType==="accepted"){

    payload.data.title="Friend Request accepted";
    payload.data.body=`${userName} accepted your friend request`;
    payload.data.click_action="com.example.abdul.pucitstudentportalsystem_TARGET_NOTIFICATION";
    
  }
 	return admin.messaging().sendToDevice(token_id,payload).then(response=>{
  	 return console.log('this is notification work');
  	 });

	

});

  });

  
  };