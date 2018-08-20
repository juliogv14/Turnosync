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
import com.juliogv14.turnosync.ui.drawerlayout.DrawerActivity;

/**
 * La clase MessageReceiver es un servicio que recibe mensajes de Firebase Cloud Messaging y muestra
 * notificaciones en la aplicación
 *
 * @author Julio García
 * @see FirebaseMessagingService
 */
public class MessageReceiver extends FirebaseMessagingService {
    /** Codigo del intent en la notificacion de actualización de calendario */
    private static final int UPDATE_REQUEST_CODE = 1;
    /** Codigo identificador de la notificacion de actualización de calendario */
    private static final int UPDATE_NOTIFICATION_ID = 1111;
    /** Codigo del intent en la notificacion de cambio de turno */
    private static final int CHANGE_REQUEST_CODE = 2;

    //@{
    /** Constantes para identificar los distintos tipos de notificaciones.
     * Se reciben con el mensaje del servicio de mensajeria.
     */
    private static final String REQUESTED = "requested";
    private static final String ACCEPTED_USER = "acceptedUser";
    private static final String ACCEPTED_MANAGER = "acceptedManager";
    private static final String APPROVED = "approved";
    private static final String CANCELLED = "cancelled";
    private static final String CONFLICT = "conflict";
    private static final String DENIED_USER = "deniedUser";
    private static final String DENIED_MANAGER = "deniedManager";
    //@}

    /**
     * Callback del ciclo de vida.
     * Se llama a onCreate de la clase de la que extiende.
     * Fuerza al servicio a esperar al debugger cuando este está activado
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if(android.os.Debug.isDebuggerConnected()) android.os.Debug.waitForDebugger();

    }

    /**
     * Callback que se llama cuando el servicio recibe un mensaje.
     * Diferencia entre tipos de actualización de calendario y tipos de cambio de turnos
     */
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
                String requestId = remoteMessage.getData().get(getString(R.string.data_key_id));
                Log.d("MessageReceiver", "Change state: " + change);
                showChangeRequest(wkName, requestId, change);
                break;

        }

    }

    /**
     * Metodo usado dentro de {@link #onMessageReceived} para mostrar la notificación de
     * actualización de turnos.
     * Crea y muestra una notificación cuando recibe un mensaje del servicio de notificaciones
     *
     * @param wkName Nombre del grupo del que se recibe notificación
     * @see android.app.Notification
     */
    private void showScheduleUpdated(String wkName) {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, UPDATE_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
        String body = String.format(getString(R.string.push_schedule_body), wkName);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.push_channel_updates_id))
                .setContentTitle(getString(R.string.push_schedule_title))
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_name);


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
        if(notificationManager != null){
            notificationManager.notify(UPDATE_NOTIFICATION_ID, notificationBuilder.build());
        }



    }

    /**
     * Metodo usado dentro de {@link #onMessageReceived} para mostrar notificaciones de cambios de
     * turnos
     * Crea y muestra una notificación cuando recibe un mensaje del servicio de notificaciones
     *
     * @param wkName Nombre del grupo del que se recibe notificación
     * @param requestId id de la solicitud de cambio ChangeRequest
     * @param changeState estado de la solicitud de cambio ChangeRequest
     * @see android.app.Notification
     * @see com.juliogv14.turnosync.data.ChangeRequest
     */
    private void showChangeRequest(String wkName, String requestId, String changeState) {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, CHANGE_REQUEST_CODE, intent, PendingIntent.FLAG_ONE_SHOT);
        String title ="";
        String body ="";
        switch (changeState){
            case REQUESTED:
                title = getString(R.string.push_change_requested_title);
                body = String.format(getString(R.string.push_change_requested_body), wkName);
                break;
            case ACCEPTED_USER:
                title = getString(R.string.push_change_acceptedUser_title);
                body = String.format(getString(R.string.push_change_acceptedUser_body), wkName);
                break;
            case ACCEPTED_MANAGER:
                title = getString(R.string.push_change_acceptedManager_title);
                body = String.format(getString(R.string.push_change_acceptedManager_body), wkName);
                break;
            case APPROVED:
                title = getString(R.string.push_change_approved_title);
                body = String.format(getString(R.string.push_change_approved_body), wkName);
                break;
            case CANCELLED:
                title = getString(R.string.push_change_cancelled_title);
                body = String.format(getString(R.string.push_change_cancelled_body), wkName);
                break;
            case CONFLICT:
                title = getString(R.string.push_change_conflict_title);
                body = String.format(getString(R.string.push_change_conflict_body), wkName);
                break;
            case DENIED_USER:
                title = getString(R.string.push_change_deniedUser_title);
                body = String.format(getString(R.string.push_change_deniedUser_body), wkName);
                break;
            case DENIED_MANAGER:
                title = getString(R.string.push_change_deniedManager_title);
                body = String.format(getString(R.string.push_change_deniedmanager_body), wkName);
                break;
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.push_channel_changes_id))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_stat_name);


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
        if(notificationManager != null){
            String numbersString = requestId.replaceAll("[^0-9]", "");
            numbersString = numbersString.length() + numbersString + numbersString.length();
            int notifId = Integer.parseInt(numbersString.substring(5));

            notificationManager.notify(notifId, notificationBuilder.build());
        }
    }
}
