package com.example.mybookshelf.notifications;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mybookshelf.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Show your notification (implementation depends on your codebase)
        showNotification(context, title, message);

        // Reschedule for the next day
        int hour = intent.getIntExtra("hour", 8);
        int minute = intent.getIntExtra("minute", 0);
        String nextMessage = intent.getStringExtra("message");

        NotificationScheduler.scheduleDailyNotification(context, hour, minute, nextMessage); // replace with your class
    }



    private void showNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                NotificationChannelManager.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title != null ? title : "Lese-Erinnerung")
                .setContentText(message != null ? message : "Zeit zum Lesen!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Prüfe, ob wir die Berechtigung für Notifications haben (erforderlich für Android 13+)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }

        // Verwende eine feste ID für tägliche Notifications
        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());

        Log.d(TAG, "Notification displayed successfully");
    }
}