package com.subhan_nadeem.sandcastle.firebase;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.subhan_nadeem.sandcastle.features.main.MainActivity;
import com.subhan_nadeem.sandcastle.R;
import com.subhan_nadeem.sandcastle.models.Message;

import java.util.List;


public class NotificationService extends com.google.firebase.messaging.FirebaseMessagingService {

    public static final String PREFIX_ROOM_TOPIC = "chat_room_";
    public static final String KEY_SERIALIZED_OBJECT = "KEY_SERIALIZED_OBJECT";
    public static final int NOTIFICATION_ID_NEW_ROOM = 1;
    public static final int NOTIFICATION_ID_NEW_MESSAGE = 0;
    public static final String KEY_NOTIFICATION_ID = "notification_id";
    private static final String TAG = "NotificationService";
    public static final String INTENT_FILTER_NEW_MESSAGE = "com.google.firebase.MESSAGING_EVENT";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            int notificationID = Integer.parseInt(remoteMessage.getData().get(KEY_NOTIFICATION_ID));
            boolean isAppInBackground = isAppIsInBackground(getApplicationContext());

            Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree(remoteMessage.getData());

            switch (notificationID) {
                case NOTIFICATION_ID_NEW_MESSAGE:
                    Message newMessage = gson.fromJson(jsonElement,
                            Message.class);

                    if (isAppInBackground)
                        sendNotification(newMessage.getUsername(),
                                newMessage.getMessage());
                    else
                        broadcastNewMessage(newMessage);
                    break;
                case NOTIFICATION_ID_NEW_ROOM:
                    if (isAppInBackground)
                        sendNotification("New Sandcastle",
                                "A new sandcastle was made close to your location!");
                    break;
            }
        }
    }

    public static IntentFilter getNewMessageIntentFilter() {
        return new IntentFilter(INTENT_FILTER_NEW_MESSAGE);
    }

    /**
     * Broadcast message to app locally
     */
    private void broadcastNewMessage(Message notification) {
        Intent intent = new Intent(INTENT_FILTER_NEW_MESSAGE);
        intent.putExtra(KEY_NOTIFICATION_ID, NOTIFICATION_ID_NEW_MESSAGE);
        intent.putExtra(KEY_SERIALIZED_OBJECT, new Gson().toJson(notification));

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    // TODO Research - appropriate to use?
    public boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses
                    = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance
                        == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(getColor(R.color.sandcastle_brown))
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}