package com.example.mybookshelf;



import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mybookshelf.apis.AiAPI;
import com.example.mybookshelf.apis.BooksAPI;
import com.example.mybookshelf.dataClass.Book;
import com.example.mybookshelf.dataClass.User;
import com.example.mybookshelf.notifications.NotificationChannelManager;
import com.example.mybookshelf.notifications.NotificationScheduler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    private GoalHandler handler;

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



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                Toast.makeText(this, "Schedule exact alarms permission not granted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                this.startActivity(intent);
                return;
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =new NotificationChannel(NotificationChannelManager.DEFAULT_CHANNEL_ID, "Main", NotificationManager.IMPORTANCE_HIGH);
            NotificationChannel serverChannel = new NotificationChannel("server_channel", "Server", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            manager.createNotificationChannel(serverChannel);
        }

        NotificationChannelManager.createNotificationChannels(this);

        NotificationScheduler.scheduleDailyNotification(this, 7, 26,"test");


        try {
            // Initialize UI components


            // Initialize Authenticator and ApiRequest
            auth = new Authenticator(this);
            uiMaster = new UIMaster(this);
            search = new Search(this);
            ai = new AiAPI();
            db = new DataBaseConnection(this);


            int uid = auth.getUid(this);
            String tempToken = auth.getToken(this);
            Log.d("MainActivity", "Token: " + tempToken);
            Log.d("MainActivity", "UID: " + uid);

            if (uid != -1){
                if (tempToken.equals(db.getToken(uid).get())){
                    logedindUser = new User(auth.getUsername(this), uid, db);
                    uiMaster.setUSer(logedindUser);
                    uiMaster.navigateToStartingPage();
                }
            }else {

                try {
                    Authenticator.clearStoredToken(this);
                    uiMaster.showLogin();

                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }


        } catch (Exception e) {
            Log.e("MainActivity", "Error during initialization", e);
            Toast.makeText(this, "An error occurred during initialization", Toast.LENGTH_SHORT).show();
        }
    }


    public void handleSearch() {
        setContentView(R.layout.main_search);
        ImageButton nav_homeBtn = findViewById(R.id.nav_home);
        ImageButton nav_StatsBtn = findViewById(R.id.nav_stats);
        ImageButton nav_GoalBtn = findViewById(R.id.nav_goals);
        ImageButton nav_SettingBtn = findViewById(R.id.nav_settings);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        searchView = findViewById(R.id.searchView);

        nav_GoalBtn.setOnClickListener(v -> uiMaster.navigateToGoals());
        nav_SettingBtn.setOnClickListener(v -> uiMaster.navigateToSettings());
        nav_StatsBtn.setOnClickListener(v -> uiMaster.setupLineChart());
        nav_homeBtn.setOnClickListener(v -> uiMaster.navigateToStartingPage());


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





    public void handleRegister(EditText usernameEditText, EditText passwordEditText,EditText reapeatPasswordEditText, EditText emailEditText) throws ExecutionException, InterruptedException {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String repeatPassword = reapeatPasswordEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(repeatPassword)){
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        auth.register(username, password, email);
        uiMaster.showLogin();
    }


    public void handleLogin(EditText usernameEditText, EditText passwordEditText, boolean stayLoggedIn) throws ExecutionException, InterruptedException {
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
                    logedindUser = new User(username, id, db);
                    uiMaster.setUSer(logedindUser);
                    LocalTime time = db.getTime(id).get();

                    if (time != null){
                        logedindUser.setHour(time.getHour());
                        logedindUser.setMinute(time.getMinute());
                    }
                    handler = new GoalHandler(logedindUser, db);
                    loadingOverlay.setVisibility(View.GONE);
                    if (stayLoggedIn){
                        String token = db.getToken(id).get();
                        auth.saveToken(this, token,id, username);
                    }

                    uiMaster.navigateToStartingPage();  // Move navigation here after user is set
                } catch (ExecutionException | InterruptedException e) {
                    loadingOverlay.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(this, "User or Password incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void saveBook(Book book) {
        FrameLayout frame = findViewById(R.id.frame);
        CardView popUp = findViewById(R.id.popupAddBook);
        Button btnSave = findViewById(R.id.saveButtonSearch);
        Button btnCancel = findViewById(R.id.cancelButtonSearch);
        Spinner spinnerScore = findViewById(R.id.scoreSpinner);
        Spinner spinnerStatus = findViewById(R.id.statusSpinner);
        EditText etPagesRead = findViewById(R.id.pagesInput);
        EditText etFiishedAt = findViewById(R.id.finishedAtInput);
        frame.setVisibility(View.VISIBLE);
        // First check if book is null

        ArrayList<String> scoreList = new ArrayList<>(List.of("none", "1 Appalling", "2 Horrible", "3 Very Bad", "4 Bad", "5 Average", "6 Fine", "7 Good", "8 Very Good", "9 Great", "10 Masterpiece"));
        ArrayList<String> statusList = new ArrayList<>(List.of("Reading","Completed","On-Hold","Dropped","Planned To Read"));

        // Create ArrayAdapters for the Spinners
        ArrayAdapter<String> scoreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scoreList);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusList);

        // Set the layout for the dropdown view (optional, can customize)
        scoreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerScore.setAdapter(scoreAdapter);
        spinnerStatus.setAdapter(statusAdapter);



        btnSave.setOnClickListener(v -> {
            if (book == null) {
                Log.e("MainActivity", "Cannot save null book");
                return;
            }

            book.setStatus(spinnerStatus.getSelectedItem().toString());

            String score = "";

            if (! spinnerScore.getSelectedItem().toString().equalsIgnoreCase("none")) {
                // Split by space and take the first part (e.g., "10" from "10 Masterpiece")
                score =  spinnerScore.getSelectedItem().toString().split(" ")[0];
            }

            LocalDate finished = null;
            if (etFiishedAt.getText().toString().isEmpty()){
                finished = LocalDate.now();
            }

            // Add book to database
            db.addBookToUser(logedindUser.getUid(), spinnerStatus.getSelectedItem().toString() ,score,finished, book.getName(), book.getAuthor(), book.getPages(),
                    book.getReleaseDate(), book.getImageUrl(), book.getDescription(), book.getPages()*1.5, book.getGenre());

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
                handler.handleBookAdded(book);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Error retrieving book from database", e);
                // Since we couldn't get the book from the database, use the original book object
                book.setInDatabase(true);
                logedindUser.addBook(book, this);
            }
            etPagesRead.getText().clear();
            etPagesRead.getText().clear();
            frame.setVisibility(View.GONE);
        });

        btnCancel.setOnClickListener(v ->{
            etPagesRead.getText().clear();
            etPagesRead.getText().clear();
            frame.setVisibility(View.GONE);
        });
    }


    public void removeBook(Book book) throws SQLException {
        Context context = getBaseContext();
        handler.handleBookRemoved(book);
        db.removeBookFromUser(book,logedindUser.getUid());
        uiMaster.reduceTimeSpendReading(book.getPages(), timeSpentReadingTextView);
        logedindUser.removeBook(book, context, findViewById(R.id.rvmyList));
    }

    public User getUser() {
        return logedindUser;
    }


    //TODO AI Response Handeling !!REMOVE LATER!!
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

    public void handleBookChange(Book book) {
        db.updateReadingStatus(logedindUser.getUid(), book.getId(), book.getStatus());
        handler.handleBookChange(book);
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