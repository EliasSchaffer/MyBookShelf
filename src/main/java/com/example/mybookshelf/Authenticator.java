package com.example.mybookshelf;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class Authenticator {
    private final DataBaseConnection db;
    private final Context context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // To post to UI thread

    public Authenticator(Context context) {
        this.context = context;
        db = new DataBaseConnection(context);
    }

    // Async login method with callback
    public void checkLogin(User attempt, BiConsumer<Boolean, Integer> callback) {
        executorService.execute(() -> {
            try {
                // Get the User object and check login
                int id = db.checkLogin(attempt.getUser(), attempt.getPassword().toCharArray()).get(); // Blocking wait

                if (id != 0) {
                    // Login successful
                    postToMain(() -> {
                        showDebugPopup("Login Successful!");
                        callback.accept(true, id); // Pass the entire User object
                    });
                } else {
                    // Login failed
                    postToMain(() -> {
                        showDebugPopup("Incorrect Username or Password!");
                        callback.accept(false, null);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                postToMain(() -> {
                    showDebugPopup("Login failed: " + e.getMessage());
                    callback.accept(false, null);
                });
            }
        });
    }








    // Use Handler to post to main thread
    private void postToMain(Runnable task) {
        mainHandler.post(task);
    }

    // Show debug popup (AlertDialog)
    private void showDebugPopup(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Debug Info")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Optional: Toast for fast messages
    private void showDebugToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Simple registration method
    public void register(String username, String password, String email) {
        showDebugPopup("Registering: " + username + " / " + email);
        db.addUser(username, password, email);
    }
}
