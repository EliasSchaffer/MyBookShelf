package com.example.mybookshelf.localServer;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import fi.iki.elonen.NanoHTTPD;

public class LocalServer extends NanoHTTPD {
    private Context context;

    public LocalServer(Context context) {
        super(8080); // Port
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        // Example endpoint to trigger local notifications
        if (session.getUri().equals("/sendNotification")) {
            // Trigger notification
            sendNotification("Hello", "This is a local notification!");
            return newFixedLengthResponse("Notification Sent!");
        }
        return newFixedLengthResponse("Invalid Request");
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), "default")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(context, NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(1002, builder.build());
        }
    }
}

