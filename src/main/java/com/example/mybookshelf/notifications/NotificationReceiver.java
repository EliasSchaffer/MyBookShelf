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

import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String notificationType = intent.getStringExtra("notificationType");

        // Show the notification
        showNotification(context, title, message);

        // Reschedule the notification based on its type
        if (notificationType != null) {
            switch (notificationType) {
                case "daily":
                    int dailyHour = intent.getIntExtra("hour", 20);
                    int dailyMinute = intent.getIntExtra("minute", 0);
                    NotificationScheduler.scheduleDailyNotification(context, dailyHour, dailyMinute, message);
                    break;

                case "weekly":
                    int weeklyDayOfWeek = intent.getIntExtra("dayOfWeek", Calendar.SUNDAY);
                    int weeklyHour = intent.getIntExtra("hour", 18);
                    int weeklyMinute = intent.getIntExtra("minute", 0);
                    NotificationScheduler.scheduleWeeklyNotification(context, weeklyDayOfWeek, weeklyHour, weeklyMinute, message);
                    break;

                case "monthly":
                    int monthlyDay = intent.getIntExtra("dayOfMonth", 1);
                    int monthlyHour = intent.getIntExtra("hour", 10);
                    int monthlyMinute = intent.getIntExtra("minute", 0);
                    NotificationScheduler.scheduleMonthlyNotification(context, monthlyDay, monthlyHour, monthlyMinute, message);
                    break;
            }
        }
    }

    private void showNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                NotificationChannelManager.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title != null ? title : "Reminder")
                .setContentText(message != null ? message : "Time to check your books!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check if we have notification permission (required for Android 13+)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Notification permission not granted");
            return;
        }

        // Use a hashcode of the title and message for a unique notification ID
        int notificationId = (title + message).hashCode();
        notificationManager.notify(notificationId, builder.build());
    }
}

