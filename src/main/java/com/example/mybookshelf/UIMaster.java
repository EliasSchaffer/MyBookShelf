package com.example.mybookshelf;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.mybookshelf.apis.AiAPI;
import com.example.mybookshelf.apis.BooksAPI;
import com.example.mybookshelf.dataClass.Book;
import com.example.mybookshelf.dataClass.User;
import com.example.mybookshelf.notifications.NotificationScheduler;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class UIMaster {

    MainActivity mainActivity;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText repeatPassword;
    private Button loginButton;
    private Button registerButton;
    private Button switchToLoginButton;
    private Button switchToRegisterButton;

    private int timeSpentReading = 0;
    private ImageButton nav_searchBtn;
    private ImageButton nav_StatsBtn;
    private TextView timeSpentReadingTextView;
    private BooksAPI booksAPI;
    private AiAPI ai;
    private DataBaseConnection db;
    private User logedindUser;
    private Map<View, Book> bookViewMap;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String AIprompt = "";
    private BookRecommendationFlow brf;


    public UIMaster(MainActivity main){
        booksAPI = new BooksAPI();
        ai = new AiAPI();
        mainActivity = main;
        db = new DataBaseConnection(mainActivity);
        bookViewMap = new HashMap<>();
    }

    public void setUSer(User user){
        this.logedindUser = user;
    }

    public void reduceTimeSpendReading(int time, TextView timeSpentReadingTextView){

        timeSpentReading-=(time*1.5);
        if (timeSpentReadingTextView != null) {
            mainActivity.runOnUiThread(() ->
                    timeSpentReadingTextView.setText("Time Spent Reading: " + + (timeSpentReading / (24 * 60)) + " d, " + ((timeSpentReading % (24 * 60)) / 60) + " h " + ((timeSpentReading % (24 * 60)) % 60) + " min")
            );
        }
    }



    public void createBookBox(LinearLayout container, Book book, boolean isSearch) {
        if (mainActivity == null) {
            Log.e("UIMaster", "MainActivity is null. Cannot create book box.");
            return;
        }

        if (book == null) {
            Log.w("createBookBox", "Received null book object");
            return;
        }

        String name = book.getName();

        // Create a container for the book box with rounded corners (using FrameLayout for button positioning)
        FrameLayout bookBox = new FrameLayout(mainActivity); // Use FrameLayout for absolute button positioning
        bookBox.setPadding(16, 16, 16, 16);
        bookBox.setBackgroundColor(Color.WHITE);

        // Apply rounded corners
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(24);
        bookBox.setBackground(background);

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        boxParams.setMargins(12, 12, 12, 12);
        bookBox.setLayoutParams(boxParams);

        // Add/Remove Button (Always in Top-Right)
        ImageButton actionButton = isSearch ? getBtnAdd(book) : getBtnRemove(book);
        if (actionButton != null) {
            if (actionButton.getParent() != null) {
                ((ViewGroup) actionButton.getParent()).removeView(actionButton);
            }
            actionButton.setVisibility(View.VISIBLE);
            actionButton.setImageResource(isSearch ? android.R.drawable.ic_input_add : android.R.drawable.ic_delete);

            // Set button size and position it at the top-right
            FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(80, 80);
            btnParams.gravity = Gravity.TOP | Gravity.END; // Position it in the top-right
            btnParams.setMargins(12, 12, 0, 0); // Top and right margin
            actionButton.setLayoutParams(btnParams);

            // Add the button to the book box (FrameLayout)
            bookBox.addView(actionButton);
        }

        // Create a vertical container to manage all content below the button
        LinearLayout verticalContainer = new LinearLayout(mainActivity);
        verticalContainer.setOrientation(LinearLayout.VERTICAL);
        verticalContainer.setPadding(12, 12, 12, 12);

        // Create a horizontal layout for image + text (book details)
        LinearLayout horizontalLayout = new LinearLayout(mainActivity);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setPadding(12, 12, 12, 12);
        horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Create an ImageView for the book cover
        ImageView bookImage = new ImageView(mainActivity);
        bookImage.setId(View.generateViewId());
        String imageUrl = book.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(mainActivity).load(imageUrl).into(bookImage);
        }

        // Set ImageView layout params
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(120, 160);
        bookImage.setLayoutParams(imageParams);

        if (isSearch){
            String author = book.getRelease_date();
            String releaseDate = book.getAuthor();
            book.setAuthor(author);
            book.setRelease_date(releaseDate);
        }


        // Create a TextView for the book details
        TextView bookDetails = new TextView(mainActivity);
        StringBuilder details = new StringBuilder();
        details.append(TextUtils.isEmpty(book.getName()) ? "Unknown" : book.getName()).append("\n");
        details.append("Author: ").append(TextUtils.isEmpty(book.getAuthor()) ? "Unknown" : book.getAuthor()).append("\n");

        bookDetails.setText(details.toString());
        bookDetails.setTextColor(Color.BLACK);
        bookDetails.setTextSize(14);
        bookDetails.setPadding(12, 0, 0, 0);

        // Add Image and Details to horizontal layout
        horizontalLayout.addView(bookImage);
        horizontalLayout.addView(bookDetails);

        // Add the horizontal layout to the vertical container
        verticalContainer.addView(horizontalLayout);

        // Add Notes input (Only if NOT in search mode)
        if (!isSearch) {
            String note;

            try {
                note = db.getNotesFromUser(logedindUser.getUid(), book.getId()).get();
            } catch (NullPointerException e) {
                // Skip adding the note field if a NullPointerException occurs
                return;
            } catch (ExecutionException | InterruptedException e) {
                // For all other exceptions, set default note text
                note = "Add notes here...";
            }

            EditText noteField = new EditText(mainActivity);

            if (note == null || note.isEmpty() || note.equals("Add notes here...")) {
                noteField.setHint("Add notes here...");
            } else {
                noteField.setText(note);
            }

            noteField.setTextColor(Color.BLACK);
            noteField.setBackgroundColor(Color.LTGRAY);
            noteField.setPadding(8, 8, 8, 8);
            noteField.setTextSize(14);
            noteField.setId(View.generateViewId());

            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.setMargins(12, 12, 12, 12);
            noteField.setLayoutParams(noteParams);

            noteField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    db.notesChanged(logedindUser.getUid(), book.getId(), s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            verticalContainer.addView(noteField);
        }


        // Add the vertical container (with all content) inside the book box
        bookBox.addView(verticalContainer);

        bookBox.setOnClickListener(v -> {
            navigateToDetails(book);
        });

        // Finally, add the book box to the main container
        container.addView(bookBox);
        bookViewMap.put(bookBox, book);


    }

    @NonNull
    private ImageButton getBtnAdd(Book book) {
        ImageButton btnAdd = new ImageButton(mainActivity);
        btnAdd.setOnClickListener(v -> mainActivity.saveBook(book));

        // Set image resource (replace `android.R.drawable.ic_menu_delete` with your actual drawable resource)
        btnAdd.setImageResource(android.R.drawable.ic_menu_add);

        // Set background to transparent to avoid default button styling
        btnAdd.setBackgroundColor(Color.TRANSPARENT); // Use transparent background instead of null

        // Set layout parameters for the button with a slightly larger size
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                48, 48 // Slightly larger size for the remove button (increased from 40x40)
        );
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnAdd.setLayoutParams(btnParams);

        return btnAdd;
    }

    @NonNull
    private ImageButton getBtnRemove(Book book) {
        ImageButton btnRemove = new ImageButton(mainActivity);
        btnRemove.setOnClickListener(v -> {
            try {
                // Remove the book from the database
                mainActivity.removeBook(book);

                // Get the parent container (LinearLayout) where the book box is located
                FrameLayout bookBox = (FrameLayout) btnRemove.getParent();  // This assumes the bookBox is the direct parent of the button
                if (bookBox != null) {
                    ViewGroup parentContainer = (ViewGroup) bookBox.getParent(); // Get the container (LinearLayout)
                    if (parentContainer != null) {
                        parentContainer.removeView(bookBox); // Remove the book box from the container
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        // Set image resource for remove button
        btnRemove.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

        // Set background to transparent to avoid default button styling
        btnRemove.setBackgroundColor(Color.TRANSPARENT);

        // Set layout parameters for the button
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                48, 48
        );
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnRemove.setLayoutParams(btnParams);

        return btnRemove;
    }




    private BarChart barChart;


    public void clearUI(LinearLayout container){
        container.removeAllViews();
    }

    public void updateReadingTime(int pages, TextView timeSpentReadingTextView){
        timeSpentReading += (pages*1.5);
        if (timeSpentReadingTextView != null) {
            mainActivity.runOnUiThread(() ->
                    timeSpentReadingTextView.setText("Time Spent Reading: " + + (timeSpentReading / (24 * 60)) + " d, " + ((timeSpentReading % (24 * 60)) / 60) + " h " + ((timeSpentReading % (24 * 60)) % 60) + " min")
            );
        }
    }

    public void showLogin() throws ExecutionException, InterruptedException {
        mainActivity.setContentView(R.layout.main_login);
        usernameEditText = mainActivity.findViewById(R.id.txfUser);
        passwordEditText = mainActivity.findViewById(R.id.txfPassword);
        loginButton = mainActivity.findViewById(R.id.btnLogin);
        switchToRegisterButton = mainActivity.findViewById(R.id.btnRegister);


        loginButton.setOnClickListener(v -> {
            try {
                mainActivity.handleLogin(usernameEditText, passwordEditText);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        switchToRegisterButton.setOnClickListener(v -> {
            showRegister();
            });

    }

    public void showRegister(){
        mainActivity.setContentView(R.layout.main_register);
        usernameEditText = mainActivity.findViewById(R.id.txfUser);
        passwordEditText = mainActivity.findViewById(R.id.txfNewPassword);
        repeatPassword = mainActivity.findViewById(R.id.txfRepeatPassword);
        emailEditText = mainActivity.findViewById(R.id.txfEmail);
        registerButton = mainActivity.findViewById(R.id.btnRegister);
        switchToLoginButton = mainActivity.findViewById(R.id.btnBackLogin);


        registerButton.setOnClickListener(v -> {
            try {
                mainActivity.handleRegister(usernameEditText, passwordEditText,repeatPassword, emailEditText);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        switchToLoginButton.setOnClickListener(v -> {
            try {
                showLogin();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setupLineChart() {
        mainActivity.setContentView(R.layout.main_chart);
        DataBaseConnection db = new DataBaseConnection(mainActivity);
        barChart = mainActivity.findViewById(R.id.barChart);
        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        ImageButton nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);

        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
        nav_StatsBtn.setOnClickListener(v -> setupLineChart());
        nav_homeBtn.setOnClickListener(v -> {
            navigateToStartingPage();
        });

        // Asynchronously fetch reading time data
        Future<ArrayList<BarEntry>> futureEntries = db.getReadingTimeByMonthAsync(mainActivity.getUser().getUid());

        // Wait for the result (you can do this in a background thread or asynchronously as well)
        try {
            // Get the result from the Future (blocking until the result is available)
            ArrayList<BarEntry> entries = futureEntries.get(); // This will block until the data is fetched

            // Daten von Minuten in Stunden umwandeln
            ArrayList<BarEntry> entriesInHours = new ArrayList<>();
            for (BarEntry entry : entries) {
                // Zeit von Minuten in Stunden umrechnen (Division durch 60)
                float hoursValue = entry.getY() / 60f;
                entriesInHours.add(new BarEntry(entry.getX(), hoursValue));
            }

            // Create the dataset for the bar chart
            BarDataSet dataSet = new BarDataSet(entriesInHours, "Reading Time (Hours)");
            dataSet.setColor(mainActivity.getResources().getColor(android.R.color.holo_blue_light));
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.4f);

            // Set the data to the bar chart
            barChart.setData(barData);

            // X-Achse anpassen - Monate als Text anzeigen
            XAxis xAxis = barChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);

            // Formatierung für die X-Achse, um Monate statt YYYYMM-Zahlen anzuzeigen
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    // Den Monatswert aus dem BarEntry-X-Wert extrahieren (YYYYMM-Format)
                    int yearMonth = (int) value;
                    if (yearMonth == 0) return ""; // Falls keine Daten
                    // YYYYMM in Jahr und Monat aufteilen
                    int year = yearMonth / 100;
                    int month = yearMonth % 100;
                    // Monatsnamen basierend auf der Nummer zurückgeben
                    String[] monthNames = {"Januar", "Februar", "März", "April", "Mai", "Juni",
                            "Juli", "August", "September", "Oktober", "November", "Dezember"};
                    if (month >= 1 && month <= 12) {
                        return monthNames[month-1] + " " + year;
                    } else {
                        return "Ungültig";
                    }
                }
            });

            // Y-Achse anpassen für Stundenanzeige
            YAxis yAxis = barChart.getAxisLeft();
            yAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.1f h", value);
                }
            });

            // Set the chart description and animate
            barChart.getDescription().setText("Reading Time in Hours");
            barChart.animateY(1000);

            // Invalidate the chart to refresh
            barChart.invalidate();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.err.println("Error fetching reading time data: " + e.getMessage());
        }
    }

    public void navigateToStartingPage() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            // Do any data processing in background
            List<Book> userBooks = logedindUser.getBookList();

            mainActivity.runOnUiThread(() -> {
                // Now do UI setup
                mainActivity.setContentView(R.layout.main_home);
                LinearLayout bookContainer = mainActivity.findViewById(R.id.bookContainer);
                nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
                nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);
                nav_StatsBtn.setOnClickListener(v -> setupLineChart());
                ImageButton searchBtn = mainActivity.findViewById(R.id.btnSearchInList);
                nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
                searchBtn.setOnClickListener(v -> handleUserSearch());

                TextView user = mainActivity.findViewById(R.id.current_user);
                user.setText("Hallo, " + logedindUser.getUser() + " \uD83D\uDC4B");
            });

            if (userBooks != null && !userBooks.isEmpty()) {
                for (Book book : userBooks) {
                    if (book != null && !TextUtils.isEmpty(book.getName())) {
                        if (book.isInDatabase()) {
                            // UI update must be on main thread
                            mainActivity.runOnUiThread(() -> {
                                LinearLayout bookContainer = mainActivity.findViewById(R.id.bookContainer);
                                createBookBox(bookContainer, book, false);
                                timeSpentReadingTextView = mainActivity.findViewById(R.id.etfTimeSpentReading);
                                updateReadingTime(book.getPages(), timeSpentReadingTextView);
                            });
                        } else {
                            // Book will be fetched asynchronously by the API callback
                            booksAPI.getOneBook(book.getName(), new BooksAPI.BookCallback() {
                                @Override
                                public void onBookFetched(List<Book> books) {
                                    mainActivity.runOnUiThread(() -> {
                                        LinearLayout bookContainer = mainActivity.findViewById(R.id.bookContainer);
                                        if (books != null && !books.isEmpty()) {
                                            Book fetched = books.get(0);
                                            mainActivity.saveBook(fetched);
                                            createBookBox(bookContainer, fetched, false);
                                            timeSpentReadingTextView = mainActivity.findViewById(R.id.etfTimeSpentReading);
                                            updateReadingTime(fetched.getPages(), timeSpentReadingTextView);
                                        } else {
                                            createBookBox(bookContainer, new Book("An Error occurred, please try again", "0", 0, "NA"), false);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            } else {
                Log.w("MainActivity", "User's book list is null or empty.");
            }
        });
    }



    void navigateToDetails(Book book) {
        mainActivity.setContentView(R.layout.main_detail);

        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        TextView txtAutor = mainActivity.findViewById(R.id.txtAuthor);
        TextView txtTitle = mainActivity.findViewById(R.id.txtTitle);
        TextView txtDescription = mainActivity.findViewById(R.id.txtDescription);
        TextView txtPages = mainActivity.findViewById(R.id.txtPageCount);
        TextView txtGenre = mainActivity.findViewById(R.id.txtGenre);
        RatingBar rbRating = mainActivity.findViewById(R.id.rbRating);
        ImageView imgCover = mainActivity.findViewById(R.id.imgCover);
        ImageButton btnPopup = mainActivity.findViewById(R.id.btnPopup);
        CardView popupWindow = mainActivity.findViewById(R.id.popupWindow);
        GridLayout chat = mainActivity.findViewById(R.id.grdChat);
        brf = new BookRecommendationFlow(mainActivity, this, book.getName());


        nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);
        nav_StatsBtn.setOnClickListener(v -> setupLineChart());

        btnPopup.setOnClickListener(v -> {
            if (popupWindow.getVisibility() == View.VISIBLE) {
                popupWindow.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> popupWindow.setVisibility(View.GONE))
                        .start();
            } else {
                // Set pivot to bottom right
                popupWindow.setPivotX(popupWindow.getWidth());
                popupWindow.setPivotY(popupWindow.getHeight());

                popupWindow.setScaleX(0f);
                popupWindow.setScaleY(0f);
                popupWindow.setAlpha(0f);
                popupWindow.setVisibility(View.VISIBLE);

                popupWindow.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(300)
                        .start();



                RelativeLayout box = mainActivity.findViewById(R.id.inputBox);
                box.removeAllViews();
                chat.removeAllViews();

                chat.setColumnCount(2);


                brf.addChatMessage("Would you like recommendations based on the book you are currently watching or a completly fresh start?", "question");

                GridLayout layout = new GridLayout(mainActivity);
                layout.setColumnCount(2);
                layout.setRowCount(1);

                // Add styled buttons
                layout.addView(createStyledGridButton("Fresh Start", v1 -> {
                    brf.addChatMessage("Fresh Start", "user");
                    brf.handleAI("freshStart");
                }));
                layout.addView(createStyledGridButton("Book Based", v1 -> {
                    brf.addChatMessage("Book Based", "user");
                    brf.handleAI("bookBased");
                }));

                // Stick layout to bottom
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layout.setLayoutParams(layoutParams);

                box.addView(layout);



            }

        });

        txtAutor.setText(book.getAuthor());
        txtTitle.setText(book.getName());
        txtDescription.setText(book.getDescription());
        txtPages.setText(String.valueOf(book.getPages()));
        txtGenre.setText(book.getGenre());
        float rating;
        try {
            rating = db.getRatingFromBook(book).get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        rbRating.setRating(rating);



        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());

        nav_homeBtn.setOnClickListener(v -> {
            navigateToStartingPage();
        });


        Glide.with(mainActivity).load(book.getImageUrl()).into(imgCover);

    }

    public void handleUserSearch() {
        SearchView searchView = mainActivity.findViewById(R.id.searchView);
        Spinner spinnerGenre = mainActivity.findViewById(R.id.spinnerGenre);
        Spinner spinnerAuthor = mainActivity.findViewById(R.id.spinnerAuthor);
        ImageButton searchBtn = mainActivity.findViewById(R.id.btnSearchInList);
        ImageButton closeSearch = mainActivity.findViewById(R.id.btnCloseSearch);
        LinearLayout searchContainer = mainActivity.findViewById(R.id.searchContainer);

        // Show the container
        searchContainer.setVisibility(View.VISIBLE);

        // Optional: these lines are only needed if you're toggling specific elements manually
        searchView.setVisibility(View.VISIBLE);
        closeSearch.setVisibility(View.VISIBLE);
        searchBtn.setVisibility(View.GONE);
        fillSpinners();
        closeSearch.setOnClickListener(v -> {
            searchContainer.setVisibility(View.GONE);
            searchBtn.setVisibility(View.VISIBLE);
            closeSearch.setVisibility(View.GONE);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterBooks(query.trim(), spinnerAuthor.getSelectedItem().toString(), spinnerGenre.getSelectedItem().toString());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBooks(newText.trim(), spinnerAuthor.getSelectedItem().toString(), spinnerGenre.getSelectedItem().toString());
                return true;
            }
        });

        spinnerAuthor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterBooks("", spinnerAuthor.getSelectedItem().toString(), spinnerGenre.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void filterBooks(String titleQuery, String selectedAuthor, String selectedGenre) {
        for (Map.Entry<View, Book> entry : bookViewMap.entrySet()) {
            View bookView = entry.getKey();
            Book book = entry.getValue();

            boolean matchesTitle = book.getName().toLowerCase().contains(titleQuery.toLowerCase());
            boolean matchesAuthor = selectedAuthor.equals("All") || book.getAuthor().equalsIgnoreCase(selectedAuthor);
            boolean matchesGenre = selectedGenre.equals("All") || book.getGenre().equalsIgnoreCase(selectedGenre);

            if (matchesTitle && matchesAuthor && matchesGenre) {
                bookView.setVisibility(View.VISIBLE);
            } else {
                bookView.setVisibility(View.GONE);
            }
        }
    }

    public void fillSpinners() {
        // Get authors and genres from the loggedinUser
        Set<String> authorsSet = logedindUser.getAuthors(); // assuming this is a Set<String>
        Set<String> genresSet = logedindUser.getGenres();   // assuming this is a Set<String>

        // Convert the Sets to ArrayLists
        List<String> authorsList = new ArrayList<>(authorsSet);
        List<String> genresList = new ArrayList<>(genresSet);

        // Add a default option (e.g., "All") to the lists
        authorsList.add(0, "All");
        genresList.add(0, "All");

        // Create ArrayAdapters for the Spinners
        ArrayAdapter<String> authorAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, authorsList);
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, genresList);

        // Set the layout for the dropdown view (optional, can customize)
        authorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapters to the Spinners
        Spinner spinnerAuthor = mainActivity.findViewById(R.id.spinnerAuthor);
        Spinner spinnerGenre = mainActivity.findViewById(R.id.spinnerGenre);

        spinnerAuthor.setAdapter(authorAdapter);
        spinnerGenre.setAdapter(genreAdapter);
    }

    private Button createStyledGridButton(String text, View.OnClickListener listener) {
        Button btn = new Button(mainActivity);
        btn.setText(text);
        btn.setBackgroundColor(Color.parseColor("#6200EE"));
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(16f);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(20, 20, 20, 20);

        btn.setLayoutParams(params);
        btn.setPadding(30, 30, 30, 30);
        btn.setOnClickListener(listener);
        return btn;
    }

    public void navigateToGoals(){
        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        ImageButton nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);
        Button addGoal = mainActivity.findViewById(R.id.btnAddGoal);
        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
        nav_StatsBtn.setOnClickListener(v -> setupLineChart());
        nav_homeBtn.setOnClickListener(v -> navigateToStartingPage());

        addGoal.setOnClickListener(v -> {
            String goalType;

            //TODO implement getting Userdata
            //TODO durch switch ersetzen
//            if (goalType.equals("daily")){
//                NotificationScheduler.scheduleDailyNotification();
//            }else if (goalType.equals("weekly")){
//                NotificationScheduler.scheduleWeeklyNotification();
//            } else {
//                NotificationScheduler.scheduleMonthlyNotification();
//            }
        });


    }
}