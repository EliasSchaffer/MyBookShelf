package com.example.mybookshelf;



import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import android.text.Editable;



public class MainActivity extends AppCompatActivity {

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
    private FrameLayout loadingOverlay;

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

        NotificationChannelManager.createNotificationChannels(this);




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



        try {


            search = new Search(this);
            ai = new AiAPI();
            auth = new Authenticator(this);
            uiMaster = new UIMaster(this);


            int uid = auth.getUid(this);
            String tempToken = auth.getToken(this);
            Log.d("MainActivity", "Token: " + tempToken);
            Log.d("MainActivity", "UID: " + uid);

            if (uid != -1){
                if (tempToken.equals(db.getToken(uid).get())){

                    LocalTime time = db.getTime(uid).get();
                    if (time != null) {
                        logedindUser = new User(auth.getUsername(this), uid, db, true, time.getHour(), time.getMinute(), this);
                    }else logedindUser = new User(auth.getUsername(this), uid, db, false, 0, 0, this);
                    uiMaster.setUSer(logedindUser);
                    uiMaster.navigateToStartingPage();
                }
            }else {

                try {
                    Authenticator.clearStoredToken(this);
                    uiMaster.showLogin();
                    db = new DataBaseConnection(this);
                    auth.setDb(this.db);
                    uiMaster.setDb(this.db);


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
        nav_StatsBtn.setOnClickListener(v -> uiMaster.navigateToStats());
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
        loadingOverlay = findViewById(R.id.loading_overlay);

        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        User userAttempt = new User(username, password);
        loadingOverlay.setVisibility(View.VISIBLE);
        auth.checkLogin(userAttempt, (success, id) -> {
            if (success) {
                try {
                    LocalTime time = db.getTime(id).get();
                    if (time != null) {
                        logedindUser = new User(username, id, db, true, time.getHour(), time.getMinute(), this);
                    }else logedindUser = new User(username, id, db, false, 0, 0, this);

                    uiMaster.setUSer(logedindUser);

                    handler = new GoalHandler(logedindUser, db, this);
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
        EditText etFinishedAt = findViewById(R.id.finishedAtInput);
        ImageButton datePickerButton = findViewById(R.id.datePickerButton);

        frame.setVisibility(View.VISIBLE);

        ArrayList<String> scoreList = new ArrayList<>(List.of("none", "1 Appalling", "2 Horrible", "3 Very Bad", "4 Bad", "5 Average", "6 Fine", "7 Good", "8 Very Good", "9 Great", "10 Masterpiece"));
        ArrayList<String> statusList = new ArrayList<>(List.of("Reading","Completed","On-Hold","Dropped","Planned To Read"));

        ArrayAdapter<String> scoreAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scoreList);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusList);

        scoreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerScore.setAdapter(scoreAdapter);
        spinnerStatus.setAdapter(statusAdapter);

        // KORREKTUR: Setup-Methoden VOR der Spinner-Listener Konfiguration aufrufen
        setupPagesValidation(etPagesRead, book, btnSave);

        // DatePicker Button initial deaktivieren
        datePickerButton.setEnabled(false);
        datePickerButton.setAlpha(0.5f);
        etFinishedAt.setEnabled(false);
        etFinishedAt.setAlpha(0.5f);

        // Listener für Status Spinner - Button aktivieren/deaktivieren
        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();

                if (selectedStatus.equals("Completed")) {
                    datePickerButton.setEnabled(true);
                    datePickerButton.setAlpha(1.0f);
                    etFinishedAt.setEnabled(true);
                    etFinishedAt.setAlpha(1.0f);
                } else {
                    datePickerButton.setEnabled(false);
                    datePickerButton.setAlpha(0.5f);
                    etFinishedAt.setEnabled(false);
                    etFinishedAt.setAlpha(0.5f);
                    etFinishedAt.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                datePickerButton.setEnabled(false);
                datePickerButton.setAlpha(0.5f);
                etFinishedAt.setEnabled(false);
                etFinishedAt.setAlpha(0.5f);
            }
        });

        // KORREKTUR: DatePicker Setup nach Spinner-Listener
        setupDatePicker(etFinishedAt, datePickerButton);

        btnSave.setOnClickListener(v -> {
            if (book == null) {
                Log.e("MainActivity", "Cannot save null book");
                return;
            }

            // Validierung für Pages Read
            String pagesReadText = etPagesRead.getText().toString().trim();
            if (!pagesReadText.isEmpty()) {
                try {
                    int pagesRead = Integer.parseInt(pagesReadText);
                    int maxPages = book.getPages();

                    if (pagesRead < 0) {
                        etPagesRead.setError("Seitenzahl kann nicht negativ sein");
                        etPagesRead.requestFocus();
                        return;
                    }

                    if (pagesRead > maxPages) {
                        etPagesRead.setError("Du kannst nicht mehr als " + maxPages + " Seiten gelesen haben");
                        etPagesRead.requestFocus();
                        return;
                    }
                } catch (NumberFormatException e) {
                    etPagesRead.setError("Bitte gib eine gültige Zahl ein");
                    etPagesRead.requestFocus();
                    return;
                }
            }

            // Validierung für Finished At Datum
            String selectedStatus = spinnerStatus.getSelectedItem().toString();
            LocalDate finished = null;

            if (selectedStatus.equals("Completed")) {
                String finishedAtText = etFinishedAt.getText().toString().trim();

                if (!finishedAtText.isEmpty()) {
                    try {
                        DateTimeFormatter formatter;
                        if (finishedAtText.contains(".")) {
                            formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                        } else {
                            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        }

                        finished = LocalDate.parse(finishedAtText, formatter);
                        LocalDate today = LocalDate.now();

                        if (finished.isAfter(today)) {
                            etFinishedAt.setError("Das Datum kann nicht in der Zukunft liegen");
                            etFinishedAt.requestFocus();
                            return;
                        }
                    } catch (DateTimeParseException e) {
                        etFinishedAt.setError("Bitte gib ein gültiges Datum ein (dd.MM.yyyy)");
                        etFinishedAt.requestFocus();
                        return;
                    }
                } else {
                    finished = LocalDate.now();
                }
            }

            book.setStatus(selectedStatus);

            String score = "";
            if (!spinnerScore.getSelectedItem().toString().equalsIgnoreCase("none")) {
                score = spinnerScore.getSelectedItem().toString().split(" ")[0];
            }

            // Add book to database
            db.addBookToUser(logedindUser.getUid(), selectedStatus, score, finished,
                    book.getName(), book.getAuthor(), book.getPages(), book.getReleaseDate(),
                    book.getImageUrl(), book.getDescription(), book.getPages() * 1.5, book.getGenre());

            try {
                Book savedBook = db.getBookByName(book.getName()).get();
                if (savedBook != null) {
                    savedBook.setInDatabase(true);
                    logedindUser.addBook(savedBook, this);
                } else {
                    Log.e("MainActivity", "Failed to retrieve book from database: " + book.getName());
                    book.setInDatabase(true);
                    logedindUser.addBook(book, this);
                }
                handler.handleBookAdded(book);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Error retrieving book from database", e);
                book.setInDatabase(true);
                logedindUser.addBook(book, this);
            }

            // Clear inputs und Dialog schließen
            etPagesRead.getText().clear();
            etFinishedAt.getText().clear();
            frame.setVisibility(View.GONE);
        });

        btnCancel.setOnClickListener(v -> {
            etPagesRead.getText().clear();
            etFinishedAt.getText().clear();
            frame.setVisibility(View.GONE);
        });
    }

    // KORREKTUR: Pages Validation überarbeitet
    private void setupPagesValidation(EditText etPagesRead, Book book, Button btnSave) {
        etPagesRead.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                // KORREKTUR: Leeres Feld sollte erlaubt sein (optional)
                if (text.isEmpty()) {
                    etPagesRead.setError(null);
                    btnSave.setEnabled(true); // Button aktiviert lassen
                    return;
                }

                try {
                    int pages = Integer.parseInt(text);
                    int maxPages = book.getPages();

                    if (pages < 0) {
                        etPagesRead.setError("Seitenzahl kann nicht negativ sein");
                        btnSave.setEnabled(false);
                    } else if (pages > maxPages) {
                        etPagesRead.setError("Maximum: " + maxPages + " Seiten");
                        btnSave.setEnabled(false);
                    } else {
                        etPagesRead.setError(null);
                        btnSave.setEnabled(true);
                    }
                } catch (NumberFormatException e) {
                    etPagesRead.setError("Ungültige Zahl");
                    btnSave.setEnabled(false);
                }
            }
        });
    }

    // KORREKTUR: DatePicker Setup bereinigt (keine Duplikation)
    private void setupDatePicker(EditText etFinishedAt, ImageButton datePickerButton) {
        datePickerButton.setOnClickListener(v -> {
            // KORREKTUR: Zusätzliche Sicherheitsprüfung
            if (!datePickerButton.isEnabled()) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        if (selectedDate.after(Calendar.getInstance())) {
                            Toast.makeText(this, "Das Datum kann nicht in der Zukunft liegen", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String formattedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear);
                        etFinishedAt.setText(formattedDate);
                    },
                    year, month, day
            );

            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
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

    

    public void handleBookChange(Book book) {
        db.updateReadingStatus(logedindUser.getUid(), book.getId(), book.getStatus());
        handler.handleBookChange(book);
    }



    public DataBaseConnection getDb() {
        return db;
    }

    public void setLoadingVisibility(int visibility) {
        runOnUiThread(() -> {
            loadingOverlay = findViewById(R.id.loading_overlay);
            loadingOverlay.setVisibility(visibility);
        });
    }
}