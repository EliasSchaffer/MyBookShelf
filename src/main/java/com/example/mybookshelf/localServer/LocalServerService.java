package com.example.mybookshelf.localServer;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class LocalServerService extends Service {
    private LocalServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        server = new LocalServer(this);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Required for foreground service
        Notification notification = new NotificationCompat.Builder(this, "server_channel")
                .setContentTitle("MyBookShelf Notification Manager")
                .setContentText("Running...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

