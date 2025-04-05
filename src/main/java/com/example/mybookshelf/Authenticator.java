package com.example.mybookshelf;

import android.content.Context;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class Authenticator {
    private final DataBaseConnection db;
    private final Context context;

    public Authenticator(Context context) {
        this.context = context;
        this.db = new DataBaseConnection(context);
    }

    public void checkLogin(User attempt, BiConsumer<Boolean, User> callback) {
        db.getLogin(attempt.getUser()).thenAccept(user -> {
            try {
                Log.d("AUTH", "Login attempt for username: " + attempt.getUser());
                Log.d("AUTH", "Attempt password: " + attempt.getPassword());
                Log.d("AUTH", "User object from DB: " + user);

                if (user != null && user.getPassword() != null && attempt.getPassword() != null) {
                    Log.d("AUTH", "Stored hash: " + user.getPassword());

                    BCrypt.Result bcryptResult = BCrypt.verifyer().verify(
                            attempt.getPassword().toCharArray(),
                            user.getPassword()
                    );

                    if (bcryptResult.verified) {
                        showDebugPopup("Login Successful!");
                        callback.accept(true, user);
                    } else {
                        showDebugPopup("Incorrect Password!");
                        callback.accept(false, null);
                    }
                } else if (user == null) {
                    showDebugPopup("User Not Found!");
                    callback.accept(false, null);
                } else {
                    showDebugPopup("Invalid login data (null password)!");
                    callback.accept(false, null);
                }
            } catch (Exception e) {
                showDebugPopup("Error: " + e.getMessage());
                callback.accept(false, null);
            }
        }).exceptionally(e -> {
            showDebugPopup("Error: " + e.getMessage());
            callback.accept(false, null);
            return null;
        });
    }

    public void register(String username, String password, String email) {
        new AlertDialog.Builder(context)
                .setTitle("Debug Info")
                .setMessage(username + password + email)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();

        CompletableFuture.runAsync(() -> {
            db.addUser(username, password, email);
        });
    }

    private void showDebugPopup(String message) {
        new android.os.Handler(context.getMainLooper()).post(() -> {
            new AlertDialog.Builder(context)
                    .setTitle("Debug Info")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void showDebugToast(String message) {
        new android.os.Handler(context.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
}
