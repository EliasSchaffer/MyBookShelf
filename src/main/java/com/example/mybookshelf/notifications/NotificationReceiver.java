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

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String notificationType = intent.getStringExtra("notificationType");

        // Show the notification
        showNotification(context, title, message);

        // If this is a monthly notification, reschedule it for next month
        if ("monthly".equals(notificationType)) {
            rescheduleMonthlyNotification(context, intent);
        }
    }

    private void showNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                NotificationChannelManager.DEFAULT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title != null ? title : "Reminder")
                .setContentText(message != null ? message : "You have a task.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }

        manager.notify(101, builder.build());
    }

    private void rescheduleMonthlyNotification(Context context, Intent receivedIntent) {
        // Extract parameters needed to reschedule
        int dayOfMonth = receivedIntent.getIntExtra("dayOfMonth", 1);
        int hours = receivedIntent.getIntExtra("hours", 10);
        int minutes = receivedIntent.getIntExtra("minutes", 0);
        String message = receivedIntent.getStringExtra("message");

        // Reschedule for next month
        NotificationScheduler.scheduleMonthlyNotification(context, dayOfMonth, hours, minutes, message);
    }
}