package com.example.mybookshelf;



import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;


public class MainActivity extends AppCompatActivity implements ApiResponseCallback {

    private Button addBookButton;
    private Authenticator auth;
    private BooksAPI booksAPI;
    private AiAPI ai;
    private UIMaster uiMaster;
    private Search search;
    private SearchView searchView;
    private TextView timeSpentReadingTextView;
    private Button goToStarting;
    private User logedindUser;
    DataBaseConnection db;

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }


        try {
            // Initialize UI components


            // Initialize Authenticator and ApiRequest
            auth = new Authenticator(this);
            uiMaster = new UIMaster(this);
            search = new Search(this);
            ai = new AiAPI();
            db = new DataBaseConnection(this);

            try {
                uiMaster.showLogin();

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


        } catch (Exception e) {
            Log.e("MainActivity", "Error during initialization", e);
            Toast.makeText(this, "An error occurred during initialization", Toast.LENGTH_SHORT).show();
        }
    }


    public void handleSearch() {
        setContentView(R.layout.search_activity);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        searchView = findViewById(R.id.searchView);
        goToStarting = findViewById(R.id.btnGoBack);
        goToStarting.setOnClickListener(v -> {
            try {
                uiMaster.navigateToStartingPage();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // Set up SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Called when the search button is pressed (user submits the search)
                if (!TextUtils.isEmpty(query)) {
                    search.searchByName(query, bookContainer);
                }
                return true; // Return true because we've handled the query submission
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the text in the search bar changes
                if (!TextUtils.isEmpty(newText)) {
                    uiMaster.clearUI(bookContainer);
                    search.searchByName(newText, bookContainer);
                }
                return true; // Return true because we've handled the query change
            }
        });
    }



    public void handleRegister(EditText usernameEditText, EditText passwordEditText, EditText emailEditText) throws ExecutionException, InterruptedException {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.register(username, password, email);
        uiMaster.showLogin();
    }


    public void handleLogin(EditText usernameEditText, EditText passwordEditText) throws ExecutionException, InterruptedException {

        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        User userAttempt = new User(username, password);

        auth.checkLogin(userAttempt, (success, id) -> {
            if (success) {
                try {
                    logedindUser = new User(username, "", id, db);
                    uiMaster.setUSer(logedindUser);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    uiMaster.navigateToStartingPage();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Toast.makeText(this, "User or Password incorrect", Toast.LENGTH_SHORT).show();
            }
        });


    }


    public void saveBook(Book book) {
        db.addBookToUser(logedindUser.getUid(), book.getName(), book.getAuthor(), book.getPages(), book.getReleaseDate(), book.getImageUrl(), book.getDescription(), 0);
        try {
            book = db.getBookByName(book.getName()).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        book.setInDatabase(true);
        logedindUser.addBook(book, this);

    }


    public void removeBook(Book book) throws SQLException {
        Context context = getBaseContext();
        db.removeBookFromUser(book,logedindUser.getUid());
        uiMaster.reduceTimeSpendReading(book.getPages(), timeSpentReadingTextView);
        logedindUser.removeBook(book, context, findViewById(R.id.bookContainer));
    }

    public User getUser() {
        return logedindUser;
    }


    //AI Response Handeling !!REMOVE LATER!!
    @Override
    public void onSuccess(String response) {
        // Show the response in a simple dialog when the request succeeds
        String result = "";
        // JSON string to parse

        // Define regex to extract the "response" field value (handling newlines)
        String regex = "\"response\":\"(.*?)\"";

        // Create Pattern object with DOTALL mode to capture newlines
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

        // Create matcher object
        Matcher matcher = pattern.matcher(response);

        //TODO: Add parsing the response

        result = response;
        showResponseDialog(result);
    }

    @Override
    public void onFailure(String error) {
        // Show the error in a dialog when the request fails
        showResponseDialog(error);
    }

    private void showResponseDialog(String message) {

        TextView boxReturn = findViewById(R.id.boxReturn);
        // Simple pop-up without any buttons or extra UI elements
        boxReturn.setText("");
        boxReturn.setText(message);
    }
}