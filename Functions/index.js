const functions = require('firebase-functions');
const request = require('./request');
const post = require('./post');
//const barModule = require('./bar');

exports.requestFunction = functions.database.ref('/{batch_id}/RequestNotifications/{reciever_id}/{sender_id}').onWrite(request.handler);
exports.postFunction=functions.database.ref('/{batch_id}/postNotificatoins').onWrite(post.handler);
//exports.bar = functions.database.ref('/bar').onWrite(barModule.handler);

/*
 * 'OnWrite' works as 'addValueEventListener' for android. It will fire the function
 * everytime there is some item added, removed or changed from the provided 'database.ref'
 * 'sendNotification' is the name of the function, which can be changed according to
 * your requirement
 */
