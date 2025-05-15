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

    // Request codes for different notification types
    public static final int REQUEST_CODE_DAILY = 100;
    public static final int REQUEST_CODE_WEEKLY = 101;
    public static final int REQUEST_CODE_MONTHLY = 102;

    // Shared Preferences keys
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_DAILY_ACTIVE = "daily_active";
    private static final String KEY_WEEKLY_ACTIVE = "weekly_active";
    private static final String KEY_MONTHLY_ACTIVE = "monthly_active";
    private static final String KEY_DAILY_HOUR = "daily_hour";
    private static final String KEY_DAILY_MINUTE = "daily_minute";
    private static final String KEY_DAILY_MESSAGE = "daily_message";
    private static final String KEY_WEEKLY_DAY = "weekly_day";
    private static final String KEY_WEEKLY_HOUR = "weekly_hour";
    private static final String KEY_WEEKLY_MINUTE = "weekly_minute";
    private static final String KEY_WEEKLY_MESSAGE = "weekly_message";
    private static final String KEY_MONTHLY_DAY = "monthly_day";
    private static final String KEY_MONTHLY_HOUR = "monthly_hour";
    private static final String KEY_MONTHLY_MINUTE = "monthly_minute";
    private static final String KEY_MONTHLY_MESSAGE = "monthly_message";

    public static void scheduleDailyNotification(Context context, int hour, int minute, String message) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If time already passed this week, schedule for next week
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", "Weekly Reading Check-in");
        intent.putExtra("message", message);
        intent.putExtra("notificationType", "weekly");
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            // Save notification settings
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_WEEKLY_ACTIVE, true);
            editor.putInt(KEY_WEEKLY_HOUR, hour);
            editor.putInt(KEY_WEEKLY_MINUTE, minute);
            editor.putString(KEY_WEEKLY_MESSAGE, message);
            editor.apply();

            Log.d(TAG, "Daily notification scheduled for " + calendar.getTime());
        }
    }

    public static void scheduleWeeklyNotification(Context context, int dayOfWeek, int hour, int minute, String message) {

    }

    public static void scheduleMonthlyNotification(Context context, int dayOfMonth, int hour, int minute, String message) {
        Calendar calendar = Calendar.getInstance();

        // Adjust day of month to prevent invalid dates
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int day = Math.min(dayOfMonth, maxDay);

        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If time already passed this month, schedule for next month
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.MONTH, 1);
            // Check again for max days in the new month
            maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            day = Math.min(dayOfMonth, maxDay);
            calendar.set(Calendar.DAY_OF_MONTH, day);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", "Monthly Reading Summary");
        intent.putExtra("message", message);
        intent.putExtra("notificationType", "monthly");
        intent.putExtra("dayOfMonth", dayOfMonth);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MONTHLY,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "App doesn't have SCHEDULE_EXACT_ALARM permission");
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            // Save notification settings
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_MONTHLY_ACTIVE, true);
            editor.putInt(KEY_MONTHLY_DAY, dayOfMonth);
            editor.putInt(KEY_MONTHLY_HOUR, hour);
            editor.putInt(KEY_MONTHLY_MINUTE, minute);
            editor.putString(KEY_MONTHLY_MESSAGE, message);
            editor.apply();

            Log.d(TAG, "Monthly notification scheduled for " + calendar.getTime());
        }
    }

    @SuppressLint("MissingPermission")
    public void sendNotification(Context context, Goal goal) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "my_channel_id")
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("You Completed Your Goal")
                .setContentText("You read" + goal.getGoal() + "pages")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }


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

    public static void cancelWeeklyNotification(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_WEEKLY,
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
        editor.putBoolean(KEY_WEEKLY_ACTIVE, false);
        editor.apply();

        Log.d(TAG, "Weekly notification cancelled");
    }

    public static void cancelMonthlyNotification(Context context) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_MONTHLY,
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
        editor.putBoolean(KEY_MONTHLY_ACTIVE, false);
        editor.apply();

        Log.d(TAG, "Monthly notification cancelled");
    }

    public static void restoreNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Restore daily notification if it was active
        if (prefs.getBoolean(KEY_DAILY_ACTIVE, false)) {
            int hour = prefs.getInt(KEY_DAILY_HOUR, 20);
            int minute = prefs.getInt(KEY_DAILY_MINUTE, 0);
            String message = prefs.getString(KEY_DAILY_MESSAGE, "Time to read!");
            scheduleDailyNotification(context, hour, minute, message);
        }

        // Restore weekly notification if it was active
        if (prefs.getBoolean(KEY_WEEKLY_ACTIVE, false)) {
            int dayOfWeek = prefs.getInt(KEY_WEEKLY_DAY, Calendar.SUNDAY);
            int hour = prefs.getInt(KEY_WEEKLY_HOUR, 18);
            int minute = prefs.getInt(KEY_WEEKLY_MINUTE, 0);
            String message = prefs.getString(KEY_WEEKLY_MESSAGE, "Weekly reading check-in");
            scheduleWeeklyNotification(context, dayOfWeek, hour, minute, message);
        }

        // Restore monthly notification if it was active
        if (prefs.getBoolean(KEY_MONTHLY_ACTIVE, false)) {
            int dayOfMonth = prefs.getInt(KEY_MONTHLY_DAY, 1);
            int hour = prefs.getInt(KEY_MONTHLY_HOUR, 10);
            int minute = prefs.getInt(KEY_MONTHLY_MINUTE, 0);
            String message = prefs.getString(KEY_MONTHLY_MESSAGE, "Monthly reading summary");
            scheduleMonthlyNotification(context, dayOfMonth, hour, minute, message);
        }
    }

    public static boolean isDailyNotificationActive(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DAILY_ACTIVE, false);
    }

    public static boolean isWeeklyNotificationActive(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_WEEKLY_ACTIVE, false);
    }

    public static boolean isMonthlyNotificationActive(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_MONTHLY_ACTIVE, false);
    }

    public static void scheduleYearlyNotification() {
    }
}
