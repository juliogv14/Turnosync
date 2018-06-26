package com.juliogv14.turnosync.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.ui.drawerlayout.DrawerActivity;

public class MessageReceiver extends FirebaseMessagingService {
    private static final int REQUEST_CODE = 1;
    private static final int NOTIFICATION_ID = 1111;

    public MessageReceiver() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        final String wkId = remoteMessage.getData().get(getString(R.string.data_key_workgroupid));
        final String wkName = remoteMessage.getData().get(getString(R.string.data_key_displayname));

        showNotifications(wkId, wkName);
    }

    private void showNotifications(String wkId, String wkName) {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "updates")
                .setContentTitle(getString(R.string.push_title_schedule))
                .setContentText(String.format(getString(R.string.push_body_workgroup), wkName))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_mycalendar_black_24dp);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("updates", getString(R.string.push_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.push_channel_desc));

            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }

        notificationManager.notify(0, notificationBuilder.build());

    }
}
