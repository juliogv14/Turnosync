package com.juliogv14.turnosync.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.ChangeRequest;
import com.juliogv14.turnosync.ui.drawerlayout.DrawerActivity;

import java.util.Random;

public class MessageReceiver extends FirebaseMessagingService {
    private static final int UPDATE_REQUEST_CODE = 1;
    private static final int UPDATE_NOTIFICATION_ID = 1111;
    private static final int CHANGE_REQUEST_CODE = 2;

    private static final String CHANGE_REQUESTED = ChangeRequest.REQUESTED;
    private static final String CHANGE_REQUESTED_DENIED = ChangeRequest.REQUESTED+"Denied";
    private static final String CHANGE_ACCEPTED_USER = ChangeRequest.ACCEPTED+"User";
    private static final String CHANGE_ACCEPTED_MANAGER = ChangeRequest.ACCEPTED+"Manager";
    private static final String CHANGE_ACCEPTED_DENIED = ChangeRequest.ACCEPTED+"Denied";
    private static final String CHANGE_APPROVED = ChangeRequest.APPROVED;

    public MessageReceiver() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String type = remoteMessage.getData().get(getString(R.string.push_key_type));
        String wkName = remoteMessage.getData().get(getString(R.string.data_key_displayname));
        Log.d("MessageReceiver", "Type: " + type);
        switch (type){
            case "schedule":
                showScheduleUpdated(wkName);
                break;
            case "change":
                String change = remoteMessage.getData().get(getString(R.string.push_key_change));
                Log.d("MessageReceiver", "Change state: " + change);
                showChangeRequest(wkName, change);
                break;

        }

    }

    private void showScheduleUpdated(String wkName) {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, UPDATE_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.push_channel_updates_id))
                .setContentTitle(getString(R.string.push_schedule_title))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(getString(R.string.push_schedule_body), wkName)))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_mycalendar_black_24dp);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(getString(R.string.push_channel_updates_id), getString(R.string.push_channel_updates_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.push_channel_updates_desc));

            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }

        notificationManager.notify(UPDATE_NOTIFICATION_ID, notificationBuilder.build());

    }

    private void showChangeRequest(String wkName, String changeState) {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, CHANGE_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
        String title ="";
        String body ="";
        switch (changeState){
            case CHANGE_REQUESTED:
                title = getString(R.string.push_change_requested_title);
                body = getString(R.string.push_change_requested_body);
                break;
            case CHANGE_REQUESTED_DENIED:
                title = getString(R.string.push_change_requestedDenied_title);
                body = getString(R.string.push_change_requestedDenied_body);
                break;
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.push_channel_changes_id))
                .setContentTitle(title)
                .setContentText(String.format(body, wkName))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_mycalendar_black_24dp);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(getString(R.string.push_channel_changes_id), getString(R.string.push_channel_changes_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(getString(R.string.push_channel_changes_desc));

            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
        Random notifId = new Random();
        notificationManager.notify(notifId.nextInt(100), notificationBuilder.build());

    }
}
