package com.example.mybookshelf;



import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
        setContentView(R.layout.main_search);
        ImageButton nav_homeBtn = findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = findViewById(R.id.nav_search);
        ImageButton nav_StatsBtn = findViewById(R.id.nav_stats);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        searchView = findViewById(R.id.searchView);

        nav_searchBtn.setOnClickListener(v -> handleSearch());
        nav_StatsBtn.setOnClickListener(v -> uiMaster.setupLineChart());
        nav_homeBtn.setOnClickListener(v -> {
            uiMaster.navigateToStartingPage();
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
        FrameLayout loadingOverlay = findViewById(R.id.loading_overlay);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        User userAttempt = new User(username, password);
        loadingOverlay.setVisibility(View.VISIBLE);
        auth.checkLogin(userAttempt, (success, id) -> {
            if (success) {
                try {
                    logedindUser = new User(username, "", id, db);
                    uiMaster.setUSer(logedindUser);
                    loadingOverlay.setVisibility(View.GONE);
                    uiMaster.navigateToStartingPage();  // Move navigation here after user is set
                } catch (ExecutionException | InterruptedException e) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            } else {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(this, "User or Password incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void saveBook(Book book) {
        // First check if book is null
        if (book == null) {
            Log.e("MainActivity", "Cannot save null book");
            return;
        }

        // Add book to database
        db.addBookToUser(logedindUser.getUid(), book.getName(), book.getAuthor(), book.getPages(),
                book.getReleaseDate(), book.getImageUrl(), book.getDescription(), 0, book.getGenre());

        try {
            // Get book from database
            Book savedBook = db.getBookByName(book.getName()).get();

            // Check if the database returned a valid book
            if (savedBook != null) {
                savedBook.setInDatabase(true);
                logedindUser.addBook(savedBook, this);
            } else {
                // Handle the case where the book wasn't found in the database
                // This might happen if there was an issue with the database operation
                Log.e("MainActivity", "Failed to retrieve book from database: " + book.getName());

                // Since we couldn't get the book from the database, use the original book object
                book.setInDatabase(true);
                logedindUser.addBook(book, this);
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e("MainActivity", "Error retrieving book from database", e);
            // Since we couldn't get the book from the database, use the original book object
            book.setInDatabase(true);
            logedindUser.addBook(book, this);
        }
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