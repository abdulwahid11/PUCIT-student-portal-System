package com.example.abdul.pucitstudentportalsystem;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireBaseMessagingService extends FirebaseMessagingService{
    SharedPreferences prefs;
    private static  String PREF_NAME="notifications";
    private static  int MESSAGE_ID=1;
    private static int REQUEST_NOTIFICATION=2;
    private static int NOTIFICATION=3;
    private static int POST=4;
    private static int ACCEPTED=5;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title=remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String clickAction=remoteMessage.getData().get("click_action");
        String from_user_id=remoteMessage.getData().get("from_user_id");
        String notificationType=remoteMessage.getData().get("type");
        if(notificationType==null){
            notificationType="default";
        }
        String name=remoteMessage.getData().get("name");

        Intent intent = new Intent(clickAction);
        intent.putExtra("user_id",from_user_id);
        intent.putExtra("user_name",name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        if (Build.VERSION.SDK_INT >= 26) {

            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("default",
                    "Channel name",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

// notificationId is a unique int for each notification that you must define
        if(notificationType.equals("message")){

            mBuilder.setOnlyAlertOnce(true);
            notificationManager.notify(MESSAGE_ID, mBuilder.build());
        }
        else if(notificationType.equals("request")){

            notificationManager.notify(REQUEST_NOTIFICATION, mBuilder.build());

        }
        else if (notificationType.equals("notification")){
            notificationManager.notify(NOTIFICATION, mBuilder.build());

        }
        else if(notificationType.equals("post")){
            notificationManager.notify(POST, mBuilder.build());

        }
        else if(notificationType.equals("accepted")){

            notificationManager.notify(ACCEPTED, mBuilder.build());
        }


    }
}
