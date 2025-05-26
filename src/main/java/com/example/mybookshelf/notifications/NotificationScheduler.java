package com.example.mybookshelf.notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mybookshelf.R;
import com.example.mybookshelf.dataClass.Goal;

import java.util.Calendar;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";

    // Request code für die tägliche Notification
    public static final int REQUEST_CODE_DAILY = 100;

    // Shared Preferences keys
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_DAILY_ACTIVE = "daily_active";
    private static final String KEY_DAILY_HOUR = "daily_hour";
    private static final String KEY_DAILY_MINUTE = "daily_minute";
    private static final String KEY_DAILY_MESSAGE = "daily_message";

    /**
     * Plant eine tägliche Notification zu einer bestimmten Uhrzeit
     */
    public static void scheduleDailyNotification(Context context, int hour, int minute, String message) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Wenn die Zeit heute bereits vorbei ist, für morgen planen
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", "Tägliche Lese-Erinnerung");
        intent.putExtra("message", message != null ? message : "Zeit zum Lesen!");
        intent.putExtra("notificationType", "daily");
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_DAILY,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "App doesn't have SCHEDULE_EXACT_ALARM permission");
                return;
            }

            // Setze wiederkehrenden Alarm
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );

            // Speichere Notification-Einstellungen
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_DAILY_ACTIVE, true);
            editor.putInt(KEY_DAILY_HOUR, hour);
            editor.putInt(KEY_DAILY_MINUTE, minute);
            editor.putString(KEY_DAILY_MESSAGE, message);
            editor.apply();

            Log.d(TAG, "Daily notification scheduled for " + String.format("%02d:%02d", hour, minute));
        }
    }

    /**
     * Plant eine tägliche Notification mit Standardwerten (20:00 Uhr)
     */
    public static void scheduleDailyNotification(Context context) {
        scheduleDailyNotification(context, 20, 0, "Zeit zum Lesen!");
    }

    /**
     * Ändert die Uhrzeit der täglichen Notification
     */
    public static void changeDailyNotificationTime(Context context, int hour, int minute) {
        String currentMessage = getDailyNotificationMessage(context);

        // Lösche die alte Notification
        cancelDailyNotification(context);

        // Plane neue Notification mit neuer Zeit
        scheduleDailyNotification(context, hour, minute, currentMessage);

        Log.d(TAG, "Daily notification time changed to " + String.format("%02d:%02d", hour, minute));
    }

    /**
     * Ändert die Nachricht der täglichen Notification
     */
    public static void changeDailyNotificationMessage(Context context, String message) {
        if (!isDailyNotificationActive(context)) {
            Log.w(TAG, "No daily notification active to change message");
            return;
        }

        int hour = getDailyNotificationHour(context);
        int minute = getDailyNotificationMinute(context);

        // Lösche die alte Notification
        cancelDailyNotification(context);

        // Plane neue Notification mit neuer Nachricht
        scheduleDailyNotification(context, hour, minute, message);

        Log.d(TAG, "Daily notification message changed to: " + message);
    }

    /**
     * Schaltet die tägliche Notification aus
     */
    public static void cancelDailyNotification(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_DAILY,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        // Update preferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_DAILY_ACTIVE, false);
        editor.apply();

        Log.d(TAG, "Daily notification cancelled");
    }

    /**
     * Schaltet die tägliche Notification ein/aus
     */
    public static void toggleDailyNotification(Context context) {
        if (isDailyNotificationActive(context)) {
            cancelDailyNotification(context);
        } else {
            scheduleDailyNotification(context);
        }
    }

    /**
     * Stellt die Notifications nach einem Neustart wieder her
     */
    public static void restoreNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Stelle tägliche Notification wieder her, falls sie aktiv war
        if (prefs.getBoolean(KEY_DAILY_ACTIVE, false)) {
            int hour = prefs.getInt(KEY_DAILY_HOUR, 20);
            int minute = prefs.getInt(KEY_DAILY_MINUTE, 0);
            String message = prefs.getString(KEY_DAILY_MESSAGE, "Zeit zum Lesen!");
            scheduleDailyNotification(context, hour, minute, message);
            Log.d(TAG, "Daily notification restored");
        }
    }

    // Getter-Methoden für die aktuellen Einstellungen

    /**
     * Prüft, ob die tägliche Notification aktiv ist
     */
    public static boolean isDailyNotificationActive(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DAILY_ACTIVE, false);
    }

    /**
     * Gibt die Stunde der täglichen Notification zurück
     */
    public static int getDailyNotificationHour(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DAILY_HOUR, 20);
    }

    /**
     * Gibt die Minute der täglichen Notification zurück
     */
    public static int getDailyNotificationMinute(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_DAILY_MINUTE, 0);
    }

    /**
     * Gibt die Nachricht der täglichen Notification zurück
     */
    public static String getDailyNotificationMessage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DAILY_MESSAGE, "Zeit zum Lesen!");
    }

    /**
     * Gibt die aktuelle Uhrzeit als formatierter String zurück
     */
    public static String getDailyNotificationTimeString(Context context) {
        int hour = getDailyNotificationHour(context);
        int minute = getDailyNotificationMinute(context);
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Sendet eine Notification für erreichte Ziele
     */
    @SuppressLint("MissingPermission")
    public static void sendGoalCompletedNotification(Context context, Goal goal) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                NotificationChannelManager.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Ziel erreicht!")
                .setContentText("Du hast " + goal.getGoal() + " Seiten gelesen!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(999, builder.build());
    }
}