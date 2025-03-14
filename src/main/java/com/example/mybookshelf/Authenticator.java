package com.example.mybookshelf;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;
import at.favre.lib.crypto.bcrypt.BCrypt;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Authenticator {

    private List<User> safedUsers;
    private DataBaseConnection db;
    private Context context; // Store Android context for UI messages

    public Authenticator(Context context) {
        this.context = context;
        safedUsers = new ArrayList<>();
        db = new DataBaseConnection(context);

        // Sample test users (not stored in DB)
        safedUsers.add(new User("Test", "123"));
        safedUsers.add(new User("Test1", "Hallo"));
        safedUsers.add(new User("Test2", "nnnn"));
        safedUsers.add(new User("Test3", "Buh!"));
    }

    public boolean checkLogin(User attempt) {
        Future<User> futureUser = db.getLogin(attempt.getUser()); // Get user from DB

        try {
            User user = futureUser.get(); // Blocks until result is available
            if (user != null) {
                BCrypt.Result result = BCrypt.verifyer().verify(attempt.getPassword().toCharArray(), user.getPassword());

                if (result.verified) {
                    showDebugPopup("Login Successful!"); // Success message
                    return true;
                } else {
                    showDebugPopup("Incorrect Password!"); // Wrong password message
                }
            } else {
                showDebugPopup("User Not Found!"); // User doesn't exist
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            showDebugPopup("Error: " + e.getMessage());
        }

        return false;
    }

    // Show a debug pop-up using AlertDialog
    private void showDebugPopup(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Debug Info")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Show a quick debug message using Toast
    private void showDebugToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
