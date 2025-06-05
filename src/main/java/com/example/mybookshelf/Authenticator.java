package com.example.mybookshelf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import com.example.mybookshelf.dataClass.User;

import java.security.KeyStore;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Authenticator {
    private DataBaseConnection db;
    private final MainActivity context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // To post to UI thread
    private static final String KEY_ALIAS = "com.example.mybookshelf.NB1ziSTb9ekHnFh11OhL";


    public Authenticator(MainActivity context) {
        this.context = context;
        db = context.getDb();
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


    // Simple registration method
    public void register(String username, String password, String email) {
        db.addUser(username, email, password);
    }

    public void saveToken(Context context, String token, int uid, String username) throws Exception {
        // Generate key if not exists
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore"
            );

            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                    )
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build()
            );

            keyGenerator.generateKey();
        }

        // Encrypt token
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(token.getBytes());

        // Save IV + encrypted token + uid + username
        SharedPreferences prefs = context.getSharedPreferences("secure_store", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("ENCRYPTED_TOKEN", Base64.getEncoder().encodeToString(encrypted))
                .putString("IV", Base64.getEncoder().encodeToString(iv))
                .putInt("UID", uid)
                .putString("USERNAME", username)
                .apply();
    }


    public int getUid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("secure_store", Context.MODE_PRIVATE);
        return prefs.getInt("UID", -1); // Return -1 if not found
    }


    public String getToken(Context context) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences("secure_store", Context.MODE_PRIVATE);
        String encryptedData = prefs.getString("ENCRYPTED_TOKEN", null);
        String ivString = prefs.getString("IV", null);

        if (encryptedData == null || ivString == null) return null;

        // Decrypt
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, Base64.getDecoder().decode(ivString));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decrypted);
    }

    public String getUsername(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("secure_store", Context.MODE_PRIVATE);
        return prefs.getString("USERNAME", null);
    }

    public static void clearStoredToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("secure_store", Context.MODE_PRIVATE);
        prefs.edit()
                .remove("ENCRYPTED_TOKEN")
                .remove("IV")
                .remove("UID")
                .remove("USERNAME")
                .apply();
    }

    public void setDb(DataBaseConnection db) {
        this.db = db;
    }

}
