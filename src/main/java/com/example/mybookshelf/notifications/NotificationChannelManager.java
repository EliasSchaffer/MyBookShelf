package com.example.mybookshelf.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationChannelManager {
    public static final String DEFAULT_CHANNEL_ID = "default_channel";
    public static final String SERVER_CHANNEL_ID = "server_channel";

    /**
     * Creates notification channels required for Android Oreo and above.
     */
    public static void createNotificationChannels(Context context) {
        // Only needed for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            // Create the default notification channel
            NotificationChannel defaultChannel = new NotificationChannel(
                    DEFAULT_CHANNEL_ID,
                    "Default Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            defaultChannel.setDescription("Channel for regular app notifications");

            // Create the server notification channel
            NotificationChannel serverChannel = new NotificationChannel(
                    SERVER_CHANNEL_ID,
                    "Server Notifications",
                    NotificationManager.IMPORTANCE_LOW
            );
            serverChannel.setDescription("Channel for local server notifications");

            // Register the channels with the system
            notificationManager.createNotificationChannel(defaultChannel);
            notificationManager.createNotificationChannel(serverChannel);
        }
    }
}