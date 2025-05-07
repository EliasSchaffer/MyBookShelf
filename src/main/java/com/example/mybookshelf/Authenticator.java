package com.example.mybookshelf;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.mybookshelf.dataClass.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

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

                if (id != -1) {
                    // Login successful
                    postToMain(() -> {
                        callback.accept(true, id); // Pass the entire User object
                    });
                } else {
                    // Login failed
                    postToMain(() -> {
                        callback.accept(false, null);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                postToMain(() -> {
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


    // Optional: Toast for fast messages


    // Simple registration method
    public void register(String username, String password, String email) {
        db.addUser(username, password, email);
    }
}
