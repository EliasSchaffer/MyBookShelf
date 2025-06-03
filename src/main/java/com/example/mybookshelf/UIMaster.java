package com.example.mybookshelf;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mybookshelf.LayoutManager.CostumeListAdapter;
import com.example.mybookshelf.LayoutManager.CustomGoalAdapter;
import com.example.mybookshelf.apis.AiAPI;
import com.example.mybookshelf.apis.BooksAPI;
import com.example.mybookshelf.dataClass.Book;
import com.example.mybookshelf.dataClass.Goal;
import com.example.mybookshelf.dataClass.User;
import com.example.mybookshelf.notifications.NotificationScheduler;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.mikephil.charting.formatter.ValueFormatter;

import at.favre.lib.crypto.bcrypt.BCrypt;

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
    List<Goal> goalList = new ArrayList<>();

    private CostumeListAdapter listAdapter;



    public UIMaster(MainActivity main){
        booksAPI = new BooksAPI();
        ai = new AiAPI();
        mainActivity = main;
        db = mainActivity.getDb();
        bookViewMap = new HashMap<>();
    }

    /**
     * Sets the logged-in user.
     */
    public void setUSer(User user) {
        this.logedindUser = user;
    }

    /**
     * Reduces the time spent reading by a given duration and updates the UI accordingly.
     */
    public void reduceTimeSpendReading(int time, TextView timeSpentReadingTextView) {

        timeSpentReading -= (time * 1.5);
        if (timeSpentReadingTextView != null) {
            mainActivity.runOnUiThread(() ->
                    timeSpentReadingTextView.setText("Time Spent Reading: " + +(timeSpentReading / (24 * 60)) + " d, " + ((timeSpentReading % (24 * 60)) / 60) + " h " + ((timeSpentReading % (24 * 60)) % 60) + " min")
            );
        }
    }



    /**
     * Creates a visual representation of a book as a box within a given container.
     */
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

        if (isSearch) {
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
                /**
                 * Handles text change events before they occur.
                 */
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                /**
                 * Updates notes in the database when text changes.
                 */
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    db.notesChanged(logedindUser.getUid(), book.getId(), s.toString());
                }

                @Override
                /**
                 * Handles text changes in an editable field.
                 */
                public void afterTextChanged(Editable s) {
                }
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
    /**
     * Creates and returns an ImageButton that, when clicked, calls saveBook with the given book.
     */
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
    /**
     * Creates and configures an ImageButton to remove a specified book.
     */
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


    /**
     * Clears all views from the given LinearLayout container.
     */
    public void clearUI(LinearLayout container) {
        container.removeAllViews();
    }

    /**
     * Updates the reading time based on the number of pages read and displays it in the specified TextView.
     */
    public void updateReadingTime(int pages, TextView timeSpentReadingTextView) {
        timeSpentReading += (pages * 1.5);
        if (timeSpentReadingTextView != null) {
            mainActivity.runOnUiThread(() ->
                    timeSpentReadingTextView.setText("Time Spent Reading: " + +(timeSpentReading / (24 * 60)) + " d, " + ((timeSpentReading % (24 * 60)) / 60) + " h " + ((timeSpentReading % (24 * 60)) % 60) + " min")
            );
        }
    }

    /**
     * Displays the login screen and handles user login attempts.
     */
    public void showLogin() throws ExecutionException, InterruptedException {
        mainActivity.setContentView(R.layout.main_login);
        usernameEditText = mainActivity.findViewById(R.id.txfUser);
        passwordEditText = mainActivity.findViewById(R.id.txfPassword);
        loginButton = mainActivity.findViewById(R.id.btnLogin);
        switchToRegisterButton = mainActivity.findViewById(R.id.btnRegister);
        CheckBox stayLoggedIn = mainActivity.findViewById(R.id.stayLoggedIn);


        loginButton.setOnClickListener(v -> {
            try {
                mainActivity.handleLogin(usernameEditText, passwordEditText, stayLoggedIn.isChecked());
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

    /**
     * Displays the registration screen and sets up event listeners for user interactions.
     * <p>
     * This method switches the main activity's content view to the registration layout,
     * initializes UI components, and registers click listeners for the register button
     * and the switch-to-login button. When the register button is clicked, it calls
     * {@link MainActivity#handleRegister(EditText, EditText, EditText, EditText)} to process
     * user input and register a new account. If an exception occurs during this process,
     * it throws a runtime exception. Similarly, when the switch-to-login button is clicked,
     * it attempts to show the login screen by calling {@link #showLogin()}, handling exceptions
     * in the same manner.
     */
    public void showRegister() {
        mainActivity.setContentView(R.layout.main_register);
        usernameEditText = mainActivity.findViewById(R.id.txfUser);
        passwordEditText = mainActivity.findViewById(R.id.txfNewPassword);
        repeatPassword = mainActivity.findViewById(R.id.txfRepeatPassword);
        emailEditText = mainActivity.findViewById(R.id.txfEmail);
        registerButton = mainActivity.findViewById(R.id.btnRegister);
        switchToLoginButton = mainActivity.findViewById(R.id.btnBackLogin);


        registerButton.setOnClickListener(v -> {
            try {
                mainActivity.handleRegister(usernameEditText, passwordEditText, repeatPassword, emailEditText);
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

    /**
     * Navigates to the stats page, initializes views, sets up navigation listeners, and loads chart data asynchronously.
     */
    public void navigateToStats() {
        // Set layout and initialize views
        mainActivity.setContentView(R.layout.main_chart);
        initializeViews();
        setupNavigationListeners();

        // Load chart data asynchronously without blocking UI thread
        loadChartDataAsync();
    }

    /**
     * Initializes the bar chart view by finding it in the main activity layout.
     */
    private void initializeViews() {
        barChart = mainActivity.findViewById(R.id.barChart);
        // Consider caching these views if this method is called frequently
    }

    /**
     * Sets up click listeners for navigation buttons in the main activity.
     * Each button is associated with a different navigation action:
     * - Home button navigates to the starting page.
     * - Search button triggers a search operation handled by the main activity.
     * - Goals button navigates to the goals page.
     * - Settings button navigates to the settings page.
     */
    private void setupNavigationListeners() {
        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        ImageButton nav_goalsBtn = mainActivity.findViewById(R.id.nav_goals);
        ImageButton nav_settingBtn = mainActivity.findViewById(R.id.nav_settings);

        nav_searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.handleSearch();
            }
        });

        nav_goalsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToGoals();
            }
        });

        nav_homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToStartingPage();
            }
        });

        nav_settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSettings();
            }
        });
    }

    /**
     * Asynchronously loads and displays chart data using AsyncTask.
     */
    private void loadChartDataAsync() {
        // Show loading indicator while data is being fetched
        // progressBar.setVisibility(View.VISIBLE); // Uncomment if you have a progress bar

        // Use AsyncTask or ExecutorService for background processing
        new AsyncTask<Void, Void, ChartData>() {
            @Override
            /**
             * Fetches reading time and completed books data for a user and returns it as ChartData.
             */
            protected ChartData doInBackground(Void... voids) {
                try {
                    // Fetch both reading time AND completed books data
                    Future<ArrayList<BarEntry>> readingTimeFuture = db.getReadingTimeByMonth(mainActivity.getUser().getUid());
                    // Future<ArrayList<BarEntry>> completedBooksFuture = db.getCompletedBooksByMonthAsync(mainActivity.getUser().getUid());

                    ArrayList<BarEntry> readingTimeEntries = readingTimeFuture.get();
                    // ArrayList<BarEntry> completedBooksEntries = completedBooksFuture.get();

                    // For now, using empty list for completed books - you'll need to implement the database method
                    ArrayList<BarEntry> completedBooksEntries = new ArrayList<>();

                    return new ChartData(readingTimeEntries, completedBooksEntries);
                } catch (InterruptedException e) {
                    Log.e("StatsActivity", "Interrupted while fetching chart data", e);
                    return null;
                } catch (ExecutionException e) {
                    Log.e("StatsActivity", "Error fetching chart data", e);
                    return null;
                }
            }

            @Override
            /**
             * Handles post-execution tasks, either setting up the chart with data or showing an error message.
             */
            protected void onPostExecute(ChartData chartData) {
                if (chartData != null) {
                    setupChart(chartData);
                } else {
                    showErrorMessage();
                }
                // Hide loading indicator
                // progressBar.setVisibility(View.GONE);
            }
        }.execute();
    }

    /**
     * Sets up the bar chart with reading time data in hours.
     */
    private void setupChart(ChartData chartData) {
        // Clean and convert reading time from minutes to hours
        ArrayList<BarEntry> cleanedEntries = filterValidEntries(chartData.readingTimeEntries);
        ArrayList<BarEntry> readingTimeInHours = convertMinutesToHours(cleanedEntries);

        // Create dataset for reading time
        BarDataSet readingTimeDataSet = new BarDataSet(readingTimeInHours, "Reading Time (Hours)");
        readingTimeDataSet.setColor(mainActivity.getResources().getColor(android.R.color.holo_blue_light));

        // If you have completed books data, add it here:
        // BarDataSet completedBooksDataSet = new BarDataSet(chartData.completedBooksEntries, "Completed Books");
        // completedBooksDataSet.setColor(mainActivity.getResources().getColor(android.R.color.holo_green_light));

        BarData barData = new BarData(readingTimeDataSet);
        barData.setBarWidth(0.4f);

        barChart.setData(barData);

        configureAxes();
        configureChart();

        // Calculate and display total reading time
        displayTotalReadingTime(readingTimeInHours);
    }

    /**
     * Filters out invalid BarEntry objects based on specific criteria.
     *
     * This method iterates through a list of BarEntry objects and checks each entry's year-month value.
     * It ensures that the year is between 2000 and 2030 and the month is within the range of 1 to 12.
     * Only entries meeting these conditions are added to the validEntries list, which is then returned.
     *
     * @param entries List of BarEntry objects to be filtered
     */
    private ArrayList<BarEntry> filterValidEntries(ArrayList<BarEntry> entries) {
        ArrayList<BarEntry> validEntries = new ArrayList<>();
        for (BarEntry entry : entries) {
            int yearMonth = (int) entry.getX();
            if (yearMonth > 0) { // Check if it's a valid year-month value
                int year = yearMonth / 100;
                int month = yearMonth % 100;
                // Only include entries with valid months (1-12) and reasonable years
                if (month >= 1 && month <= 12 && year >= 2000 && year <= 2030) {
                    validEntries.add(entry);
                }
            }
        }
        return validEntries;
    }

    /**
     * Converts a list of BarEntries from minutes to hours.
     */
    private ArrayList<BarEntry> convertMinutesToHours(ArrayList<BarEntry> entries) {
        ArrayList<BarEntry> hoursEntries = new ArrayList<>();
        for (BarEntry entry : entries) {
            hoursEntries.add(new BarEntry(entry.getX(), entry.getY() / 60f));
        }
        return hoursEntries;
    }

    /**
     * Configures the X and Y axes of a bar chart.
     */
    private void configureAxes() {
        // X-axis configuration
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new MonthValueFormatter());

        // Y-axis configuration for left side (reading time)
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // Prevent negative values
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f h", value);
            }
        });

        // Disable right axis for now
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
        rightAxis.setAxisMinimum(0f); // Also prevent negative values on right axis if you enable it later
    }

    /**
     * Configures the chart with default settings and animations.
     */
    private void configureChart() {
        barChart.getDescription().setText("Reading Time in Hours");
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    /**
     * Displays the total reading time by summing up entries and updating a TextView.
     */
    private void displayTotalReadingTime(ArrayList<BarEntry> entriesInHours) {
        TextView allTime = mainActivity.findViewById(R.id.txtAllTime);
        double totalTime = 0;

        for (BarEntry entry : entriesInHours) {
            totalTime += entry.getY();
        }

        totalTime = Math.round(totalTime * 100.0) / 100.0; // More precise rounding

        allTime.setText("Total Reading Time: " + totalTime + " h");
    }

    /**
     * Displays a short toast message indicating failure to load reading statistics.
     */
    private void showErrorMessage() {
        Toast.makeText(mainActivity, "Failed to load reading statistics", Toast.LENGTH_SHORT).show();
    }

    // Helper class to hold chart data
    private static class ChartData {
        final ArrayList<BarEntry> readingTimeEntries;
        final ArrayList<BarEntry> completedBooksEntries;

        ChartData(ArrayList<BarEntry> readingTimeEntries, ArrayList<BarEntry> completedBooksEntries) {
            this.readingTimeEntries = readingTimeEntries;
            this.completedBooksEntries = completedBooksEntries;
        }
    }

    // Extracted formatter class for better organization
    private static class MonthValueFormatter extends ValueFormatter {
        private static final String[] MONTH_NAMES = {
                "Januar", "Februar", "März", "April", "Mai", "Juni",
                "Juli", "August", "September", "Oktober", "November", "Dezember"
        };

        @Override
        /**
         * Converts a float value representing a year and month to its formatted string representation.
         */
        public String getFormattedValue(float value) {
            int yearMonth = (int) value;
            if (yearMonth == 0) return "";

            int year = yearMonth / 100;
            int month = yearMonth % 100;

            if (month >= 1 && month <= 12) {
                return MONTH_NAMES[month - 1] + " " + year;
            }
            return ""; // Return empty string instead of "Invalid"
        }
    }



    // Modified navigateToStartingPage method for UIMaster class
    /**
     * Navigates to the starting page, updating the UI and loading user data in the background.
     */
    public void navigateToStartingPage() {
        // Create a dedicated executor for background work
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // First update the UI immediately to improve perceived performance
        mainActivity.runOnUiThread(() -> {
            // Set the content view first for better user experience
            mainActivity.setContentView(R.layout.main_home);

            // Initialize navigation buttons
            setupNavigationButtons();

            // Update user greeting
            TextView user = mainActivity.findViewById(R.id.current_user);
            if (user != null && logedindUser != null) {
                user.setText("Hallo, " + logedindUser.getUser() + " \uD83D\uDC4B");
            }

            // Pre-configure RecyclerView for better performance
            RecyclerView recyclerView = mainActivity.findViewById(R.id.rvmyList);
            if (recyclerView != null) {
                recyclerView.setHasFixedSize(true);

                // Use a LinearLayoutManager with prefetch
                LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity);
                layoutManager.setInitialPrefetchItemCount(10); // Prefetch items
                recyclerView.setLayoutManager(layoutManager);

                // Disable predictive animations for smoother scrolling
                layoutManager.setItemPrefetchEnabled(true);

                // Use a custom item animator that minimizes work
                recyclerView.setItemAnimator(new DefaultItemAnimator() {
                    @Override
                    /**
                     * Animates a change in RecyclerView item position if within a small threshold.
                     */
                    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
                        // Skip animation for large position changes
                        if (Math.abs(fromX - toX) > 100 || Math.abs(fromY - toY) > 100) {
                            dispatchChangeFinished(oldHolder, true);
                            dispatchChangeFinished(newHolder, false);
                            return true;
                        }
                        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
                    }
                });
            }
        });

        // Then load data in background
        executor.execute(() -> {
            try {
                // Get user books with timeout to avoid ANR
                List<Book> userBooks = null;
                try {
                    // Add timeout logic if needed
                    userBooks = logedindUser.getBookList();
                } catch (Exception e) {
                    Log.e("UIMaster", "Error loading book list", e);
                }

                // Store final reference for use in lambda
                final List<Book> finalUserBooks = userBooks != null ? userBooks : new ArrayList<>();

                // Update UI on main thread with loaded data
                mainActivity.runOnUiThread(() -> {
                    RecyclerView recyclerView = mainActivity.findViewById(R.id.rvmyList);
                    if (recyclerView != null) {
                        // Create adapter with the loaded books
                        CostumeListAdapter adapter = new CostumeListAdapter(mainActivity, finalUserBooks, db, logedindUser, this);
                        recyclerView.setAdapter(adapter);

                        // Add scroll listener for lazy loading if needed
                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            /**
                             * Pauses or resumes image loading based on the scroll state of the RecyclerView.
                             */
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                // Pause image loading when fast scrolling
                                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                                    Glide.with(mainActivity).pauseRequests();
                                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                    Glide.with(mainActivity).resumeRequests();
                                }
                            }
                        });

                        // Register adapter in a variable for potential cleanup
                        listAdapter = adapter;
                    }



                    // Update reading time if needed
                    TextView timeSpentReadingTextView = mainActivity.findViewById(R.id.etfTimeSpentReading);
                    if (timeSpentReadingTextView != null && !finalUserBooks.isEmpty()) {
                        updateReadingTime(finalUserBooks.get(0).getPages(), timeSpentReadingTextView);
                    }
                });
            } catch (Exception e) {
                Log.e("UIMaster", "Error in navigateToStartingPage", e);

                // Show error message on UI thread
                mainActivity.runOnUiThread(() -> {
                    Toast.makeText(mainActivity, "Failed to load books", Toast.LENGTH_SHORT).show();
                });
            }
        });

        // Clean up executor when done
        executor.shutdown();
    }

    // Helper method to set up navigation buttons
    /**
     * Sets up the navigation buttons with their respective click listeners.
     */
    private void setupNavigationButtons() {
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        ImageButton nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);
        ImageButton nav_goals = mainActivity.findViewById(R.id.nav_goals);
        ImageButton nav_settingBtn = mainActivity.findViewById(R.id.nav_settings);
        ImageButton searchBtn = mainActivity.findViewById(R.id.btnSearchInList);

        // Set up navigation buttons
        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
        nav_StatsBtn.setOnClickListener(v -> navigateToStats());
        nav_goals.setOnClickListener(v -> navigateToGoals());
        nav_settingBtn.setOnClickListener(v -> navigateToSettings());
        searchBtn.setOnClickListener(v -> handleListSearch());
    }



    /**
     * Navigates to the details page of a selected book.
     *
     * This method sets up the UI for displaying detailed information about a book, including its title,
     * author, description, number of pages, genre, and rating. It also handles user interactions such as
     * navigating to other sections of the app, updating the reading status of the book, and initiating AI-based
     * recommendations.
     *
     * @param book The {@link Book} object containing details about the book to be displayed.
     */
    public void navigateToDetails(Book book) {
        mainActivity.setContentView(R.layout.main_detail);

        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        ImageButton nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);
        ImageButton nav_goalBtn = mainActivity.findViewById(R.id.nav_goals);
        ImageButton nav_settingBtn = mainActivity.findViewById(R.id.nav_settings);
        TextView txtAutor = mainActivity.findViewById(R.id.txtAuthor);
        TextView txtTitle = mainActivity.findViewById(R.id.txtTitle);
        TextView txtDescription = mainActivity.findViewById(R.id.txtDescription);
        TextView txtPages = mainActivity.findViewById(R.id.txtPageCount);
        TextView txtGenre = mainActivity.findViewById(R.id.txtGenre);
        RatingBar rbRating = mainActivity.findViewById(R.id.rbRating);
        ImageView imgCover = mainActivity.findViewById(R.id.imgCover);
        ImageButton btnPopup = mainActivity.findViewById(R.id.btnPopup);
        CardView popupCard = mainActivity.findViewById(R.id.popupCard);
        RelativeLayout popupRel = mainActivity.findViewById(R.id.popupRel);
        GridLayout chat = mainActivity.findViewById(R.id.grdChat);
        Spinner spinnerStatus = mainActivity.findViewById(R.id.spinnerStatus);

        brf = new BookRecommendationFlow(mainActivity, this, book.getName());


        nav_StatsBtn.setOnClickListener(v -> navigateToStats());
        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
        nav_homeBtn.setOnClickListener(v -> navigateToStartingPage());
        nav_goalBtn.setOnClickListener(v -> navigateToGoals());
        nav_settingBtn.setOnClickListener(v -> navigateToSettings());

        ArrayList<String> statusList = new ArrayList<>(List.of("Reading","Completed","On-Hold","Dropped","Planned To Read"));
        String selectedStatus = book.getStatus();

        if (statusList.contains(selectedStatus)) {
            statusList.remove(selectedStatus);
            statusList.add(0, selectedStatus);
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, statusList);
        spinnerStatus.setAdapter(statusAdapter);


        final boolean[] isSpinnerInitial = {true};

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitial[0]) {
                    // Ignore this initial callback
                    isSpinnerInitial[0] = false;
                    return;
                }

                // Now handle user selection
                book.setStatus(spinnerStatus.getSelectedItem().toString());
                mainActivity.handleBookChange(book);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        btnPopup.setOnClickListener(v -> {
            if (popupCard.getVisibility() == View.VISIBLE) {
                popupCard.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(() -> popupCard.setVisibility(View.INVISIBLE))
                        .start();
            } else {
                // First make visible but keep at 0 size
                popupCard.setVisibility(View.VISIBLE);
                popupCard.setAlpha(0f);
                popupCard.setScaleX(0f);
                popupCard.setScaleY(0f);

                // Post to wait for layout
                popupCard.post(() -> {
                    // Set pivot to bottom-right corner
                    popupCard.setPivotX(popupCard.getWidth());
                    popupCard.setPivotY(popupCard.getHeight());

                    // Animate
                    popupCard.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(1.0f))
                            .start();
                });



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





        Glide.with(mainActivity).load(book.getImageUrl()).into(imgCover);

    }

    /**
     * Handles the display and functionality of the list search interface, including animations and filtering books based on user input.
     */
    public void handleListSearch() {
        SearchView searchView = mainActivity.findViewById(R.id.searchView);
        Spinner spinnerGenre = mainActivity.findViewById(R.id.spinnerGenre);
        Spinner spinnerAuthor = mainActivity.findViewById(R.id.spinnerAuthor);
        ImageButton searchBtn = mainActivity.findViewById(R.id.btnSearchInList);
        ImageButton closeSearch = mainActivity.findViewById(R.id.btnCloseSearch);
        LinearLayout searchContainer = mainActivity.findViewById(R.id.searchContainer);

        // Make the view visible and prepare initial state
        searchContainer.setVisibility(View.VISIBLE);
        searchContainer.setAlpha(0f);
        searchContainer.setScaleX(0.95f);
        searchContainer.setScaleY(0.95f);
        searchContainer.setTranslationY(-50); // slide from -50px above (adjust as needed)

        // Animate all properties together
        searchContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator()) // Smooth acceleration/deceleration
                .start();


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
            /**
             * Filters books based on the search query and selected author/genre.
             */
            public boolean onQueryTextSubmit(String query) {
                filterBooks(query.trim(), spinnerAuthor.getSelectedItem().toString(), spinnerGenre.getSelectedItem().toString());
                return true;
            }

            @Override
            /**
             * Filters books based on the query text, author selection, and genre selection.
             */
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

    /**
     * Filters books based on title, author, and genre queries.
     *
     * This method iterates over a map of book views and their corresponding Book objects.
     * It checks if each book matches the provided title query, author, and genre filters.
     * If a book meets all criteria, its view is set to visible; otherwise, it is hidden.
     */
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

    /**
     * Fills spinners with authors and genres from the logged-in user, adding a default "All" option.
     */
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

    /**
     * Creates a styled button with specified text and click listener.
     */
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

    /**
     * Navigates to and sets up the Goals screen, allowing users to view,
     * add, and manage their reading goals.
     *
     * This method performs several key tasks:
     * - Sets the content view to the Goals activity layout.
     * - Initializes and configures a RecyclerView to display goal items.
     * - Adds click listeners for buttons that switch between displaying current,
     *   completed, and failed goals.
     * - Configures an AlertDialog with options to edit or delete selected goals.
     * - Handles adding new goals through user input, including validation
     *   of fields and database operations.
     *
     * @param mainActivity The MainActivity instance that calls this method,
     *                     used for accessing resources, context, and user data.
     */
    public void navigateToGoals() {
        mainActivity.setContentView(R.layout.main_goal);

        // Debug log
        Log.d("GoalsDebug", "Navigating to goals screen");

        // UI Elements setup
        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        ImageButton nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);
        ImageButton nav_SettingBtn = mainActivity.findViewById(R.id.nav_settings);
        Button addGoal = mainActivity.findViewById(R.id.btnAddGoal);
        Spinner spinnerGoalType = mainActivity.findViewById(R.id.spinnerStatus);
        Button cancel = mainActivity.findViewById(R.id.btnPopupCancel);
        EditText book = mainActivity.findViewById(R.id.etGoalPopupName);
        EditText number = mainActivity.findViewById(R.id.editTextNumber);
        CardView popUp = mainActivity.findViewById(R.id.popupWindow);
        Button save = mainActivity.findViewById(R.id.btnPopupSave);
        RadioGroup type = mainActivity.findViewById(R.id.frequencyRadioGroup);
        RecyclerView rvCompletedGoals = mainActivity.findViewById(R.id.rvCompletedGoals);
        Button current = mainActivity.findViewById(R.id.btnCurrent);
        Button completed = mainActivity.findViewById(R.id.btnCompleted);
        Button failed = mainActivity.findViewById(R.id.btnFailed);

        // Check if RecyclerView was found
        if (rvCompletedGoals == null) {
            Log.e("GoalsDebug", "RecyclerView not found in layout!");
            Toast.makeText(mainActivity, "Error: RecyclerView not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigation setup
        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
        nav_StatsBtn.setOnClickListener(v -> navigateToStats());
        nav_homeBtn.setOnClickListener(v -> navigateToStartingPage());
        nav_SettingBtn.setOnClickListener(v -> navigateToSettings());

        // IMPORTANT: First get the most up-to-date goals list from the user object
        if (logedindUser != null && logedindUser.getGoalList() != null) {
            goalList = new ArrayList<>(logedindUser.getGoalList());
            Log.d("GoalsDebug", "Loaded " + goalList.size() + " goals from user");
        } else {
            goalList = new ArrayList<>();
            Log.d("GoalsDebug", "No goals found, creating empty list");
        }



        // Setup RecyclerView with clear visibility
        rvCompletedGoals.setVisibility(View.VISIBLE);

        // Setup LayoutManager first
        LinearLayoutManager layoutManager = new LinearLayoutManager(mainActivity);
        rvCompletedGoals.setLayoutManager(layoutManager);

        // Create and set adapter AFTER getting updated goals
        CustomGoalAdapter goalAdapter = new CustomGoalAdapter(mainActivity, goalList, db);
        rvCompletedGoals.setAdapter(goalAdapter);

        // Set click listener for "current" button
        current.setOnClickListener(v -> {
            completed.setBackgroundResource(R.drawable.rounded_button_black);
            completed.setTextColor(mainActivity.getColor(R.color.white));
            failed.setBackgroundResource(R.drawable.rounded_button_black);
            failed.setTextColor(mainActivity.getColor(R.color.white));
            current.setBackgroundResource(R.drawable.rounded_button_grey);
            current.setTextColor(mainActivity.getColor(R.color.black));
            goalAdapter.setGoals(logedindUser.getGoalList());
        });

        // Set click listener for "completed" button
        completed.setOnClickListener(v -> {
            current.setBackgroundResource(R.drawable.rounded_button_black);
            current.setTextColor(mainActivity.getColor(R.color.white));
            failed.setBackgroundResource(R.drawable.rounded_button_black);
            failed.setTextColor(mainActivity.getColor(R.color.white));
            completed.setBackgroundResource(R.drawable.rounded_button_grey);
            completed.setTextColor(mainActivity.getColor(R.color.black));
            goalAdapter.setGoals(logedindUser.getCompletedGoalList());
        });

        // Set click listener for "failed" button
        failed.setOnClickListener(v -> {
            current.setBackgroundResource(R.drawable.rounded_button_grey);
            current.setTextColor(mainActivity.getColor(R.color.black));
            completed.setBackgroundResource(R.drawable.rounded_button_grey);
            completed.setTextColor(mainActivity.getColor(R.color.black));
            failed.setBackgroundResource(R.drawable.rounded_button_grey);
            failed.setTextColor(mainActivity.getColor(R.color.white));
            goalAdapter.setGoals(logedindUser.getFailedGoalList());
        });



        Log.d("GoalsDebug", "RecyclerView setup complete with " + goalList.size() + " items");

        // Setup goal types spinner
        Set<String> goalTypesSet = new HashSet<>(Arrays.asList("Read Books", "Read Pages", "Read Time", "Read Specific Book"));
        List<String> goalTypesList = new ArrayList<>(goalTypesSet);
        Collections.sort(goalTypesList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, goalTypesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalType.setAdapter(adapter);

        spinnerGoalType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            /**
             * Toggles visibility of book and number views based on selected item.
             */
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                if (selected.equals("Read Specific Book")) {
                    book.setVisibility(View.VISIBLE);
                    number.setVisibility(View.GONE);
                } else {
                    book.setVisibility(View.GONE);
                    number.setVisibility(View.VISIBLE);
                }
            }

            @Override
            /**
             * Placeholder method called when nothing is selected.
             */
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional
            }
        });

        // Add Goal button click listener
        addGoal.setOnClickListener(v -> {
            Log.d("GoalsDebug", "Add goal button clicked");
            TranslateAnimation slideIn = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f,   // from bottom
                    Animation.RELATIVE_TO_SELF, 0f    // to original position
            );
            slideIn.setDuration(300);
            slideIn.setInterpolator(new DecelerateInterpolator());

            popUp.startAnimation(slideIn);
            popUp.setVisibility(View.VISIBLE);
            addGoal.setVisibility(View.GONE);
        });

        // Cancel button listener
        cancel.setOnClickListener(v -> {
            Log.d("GoalsDebug", "Cancel button clicked");
            TranslateAnimation slideOut = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,    // from original position
                    Animation.RELATIVE_TO_SELF, 1f     // to bottom
            );
            slideOut.setDuration(300);
            slideOut.setInterpolator(new AccelerateInterpolator());

            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    popUp.setVisibility(View.GONE);
                    addGoal.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            popUp.startAnimation(slideOut);
        });

        // Save button listener
        save.setOnClickListener(v -> {
            Log.d("GoalsDebug", "Save button clicked");

            // Validate user selections
            String frequenzy = "";
            int id = type.getCheckedRadioButtonId();
            if (id != -1) {
                RadioButton selectedRadioButton = mainActivity.findViewById(id);
                frequenzy = selectedRadioButton.getText().toString();
                Log.d("GoalsDebug", "Selected frequency: " + frequenzy);
            } else {
                Log.e("GoalsDebug", "No frequency selected!");
                Toast.makeText(mainActivity, "Please select a frequency", Toast.LENGTH_SHORT).show();
                return;
            }

            String goalCategory = spinnerGoalType.getSelectedItem().toString();
            Log.d("GoalsDebug", "Goal category: " + goalCategory);

            Goal finalGoal = null;

            try {
                String numberStr = number.getText().toString().trim();
                int targetNumber = Integer.parseInt(numberStr);

                if (goalCategory.equals("Read Specific Book")) {
                    String bookName = book.getText().toString().trim();
                    if (bookName.isEmpty()) {
                        Log.e("GoalsDebug", "Book name is empty!");
                        Toast.makeText(mainActivity, "Please enter a book name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    finalGoal = new Goal(0,0,targetNumber, bookName, frequenzy, goalCategory);
                    Log.d("GoalsDebug", "Created book goal: " + bookName);
                } else {

                    if (numberStr.isEmpty()) {
                        Log.e("GoalsDebug", "Number field is empty!");
                        Toast.makeText(mainActivity, "Please enter a number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    finalGoal = new Goal(0,0, targetNumber, frequenzy, goalCategory);
                    Log.d("GoalsDebug", "Created numeric goal: " + targetNumber);
                }
            } catch (NumberFormatException e) {
                Log.e("GoalsDebug", "Error parsing number: " + e.getMessage());
                Toast.makeText(mainActivity, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Debug check to ensure goal is created properly
            if (finalGoal == null) {
                Log.e("GoalsDebug", "Failed to create goal object!");
                Toast.makeText(mainActivity, "Error creating goal", Toast.LENGTH_SHORT).show();
                return;
            }



            // Add goal to user first (database operation)
            // This should generate a proper ID if your database is set up correctly
            long newGoalId = db.addGoal(finalGoal, mainActivity.getUser());

            // Update the goal object with the new ID if needed
            if (newGoalId > 0) {
                finalGoal.setId((int) newGoalId);
                NotificationScheduler.scheduleOneTimeNotification(mainActivity, finalGoal.getDeadline(), Integer.parseInt(String.valueOf(newGoalId)));
                Log.d("GoalsDebug", "Goal saved to database with ID: " + newGoalId);

                // Update user's goal list AFTER successful database operation
                mainActivity.getUser().addGoal(finalGoal);

                // Update our local list to match
                goalList.add(finalGoal);
            } else {
                Log.e("GoalsDebug", "Failed to save goal to database");
                Toast.makeText(mainActivity, "Failed to save goal", Toast.LENGTH_SHORT).show();
                return;
            }


            // Now add to the displayed list after successful database operation
            int insertPosition = goalList.size() - 1;

            // Use specific notify method for better animation
            goalAdapter.notifyItemInserted(insertPosition);

            // Animate the newly added item
            RecyclerView.ViewHolder viewHolder = rvCompletedGoals.findViewHolderForAdapterPosition(insertPosition);
            if (viewHolder != null) {
                viewHolder.itemView.setAlpha(0f);
                viewHolder.itemView.setTranslationY(20f);
                viewHolder.itemView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .start();
            }

            // Scroll to the new item if needed
            rvCompletedGoals.smoothScrollToPosition(insertPosition);

            // Animation to close the popup
            TranslateAnimation slideOut = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 0f,
                    Animation.RELATIVE_TO_SELF, 1f
            );
            slideOut.setDuration(300);
            slideOut.setInterpolator(new AccelerateInterpolator());

            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                /**
                 * Called when an animation starts.
                 */
                public void onAnimationStart(Animation animation) {}

                @Override
                /**
                 * Hides the pop-up and shows the add goal button, then clears input fields.
                 */
                public void onAnimationEnd(Animation animation) {
                    popUp.setVisibility(View.GONE);
                    addGoal.setVisibility(View.VISIBLE);

                    // Clear input fields for next use
                    book.setText("");
                    number.setText("");
                    type.clearCheck();
                }

                @Override
                /**
                 * Handles the repeat event of an animation.
                 */
                public void onAnimationRepeat(Animation animation) {}
            });

            popUp.startAnimation(slideOut);
        });
    }

    /**
     * Navigates to the settings screen and sets up the UI elements and their corresponding listeners.
     *
     * This method is responsible for:
     * - Setting the content view to the main settings layout.
     * - Initializing various UI components such as buttons, text views, switches, and time pickers.
     * - Configuring listeners for UI events, including button clicks and changes in switch states.
     * - Handling user interactions for changing passwords, deleting accounts, changing usernames,
     *   changing emails, and configuring notification settings.
     *
     * @throws ExecutionException if an error occurs during asynchronous execution.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public void navigateToSettings(){
        mainActivity.setContentView(R.layout.main_setting);

        // Debug log
        Log.d("GoalsDebug", "Navigating to goals screen");

        // UI Elements setup
        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        ImageButton nav_StatsBtn = mainActivity.findViewById(R.id.nav_stats);
        ImageButton nav_GoalBtn = mainActivity.findViewById(R.id.nav_goals);
        Button btnSignOut = mainActivity.findViewById(R.id.btnSignOut);
        Button btnDelAcc = mainActivity.findViewById(R.id.btndelAcc);
        Button btnChangePasswd = mainActivity.findViewById(R.id.btnSavepasswd);
        Button btnChangeEmail = mainActivity.findViewById(R.id.btnSaveEmail);
        Button btnChangeUsername = mainActivity.findViewById(R.id.btnSaveUsername);
        Button btnSaveNotificationSettings = mainActivity.findViewById(R.id.btnSaveNotificationSettings);
        Switch mode = mainActivity.findViewById(R.id.switchdarkmode);
        TextView changePasswd = mainActivity.findViewById(R.id.tvchangePasswd);
        TextView changeUsername = mainActivity.findViewById(R.id.tvchangeUsername);
        TextView changeEmail = mainActivity.findViewById(R.id.tvchangeEmail);
        TextView notificationSettings = mainActivity.findViewById(R.id.tvNotificationSettings);
        RelativeLayout passwdChangeBox = mainActivity.findViewById(R.id.passwdChangeBox);
        RelativeLayout usernameChangeBox = mainActivity.findViewById(R.id.usernameChangeBox);
        RelativeLayout EmailChangeBox = mainActivity.findViewById(R.id.EmailChangeBox);
        RelativeLayout notificationSettingsBox = mainActivity.findViewById(R.id.notificationSettingsBox);
        EditText etPassword = mainActivity.findViewById(R.id.etpasswd);
        EditText etNewPassword = mainActivity.findViewById(R.id.etNewpasswd);
        EditText etNewPasswordRepeat = mainActivity.findViewById(R.id.etNewpasswdrepeat);
        EditText etEmail = mainActivity.findViewById(R.id.etNewEmail);
        EditText etNewUsername = mainActivity.findViewById(R.id.etNewUsername);
        CheckBox notification = mainActivity.findViewById(R.id.cbEnableNotifications);
        TimePicker timePicker = mainActivity.findViewById(R.id.timePickerNotification);

        nav_homeBtn.setOnClickListener(v -> navigateToStartingPage());
        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
        nav_StatsBtn.setOnClickListener(v -> navigateToStats());
        nav_GoalBtn.setOnClickListener(v -> navigateToGoals());

        timePicker.setIs24HourView(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(12);
            timePicker.setMinute(0);
        }

        if (NotificationScheduler.isDailyNotificationActive(mainActivity)) {
            timePicker.setHour(NotificationScheduler.getDailyNotificationHour(mainActivity));
            timePicker.setMinute(NotificationScheduler.getDailyNotificationMinute(mainActivity));
            notification.setChecked(true);
        }

        btnSignOut.setOnClickListener(v -> {
            Authenticator.clearStoredToken(mainActivity);
            try {
                showLogin();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        });

        btnDelAcc.setOnClickListener(v ->{
            Authenticator.clearStoredToken(mainActivity);
            new AlertDialog.Builder(mainActivity)
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to do this?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            db.deleteUser(logedindUser.getUid());
                            try {
                                showLogin();
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        });

        changePasswd.setOnClickListener(v -> toggleVisibilityAnimated(passwdChangeBox));
        changeUsername.setOnClickListener(v -> toggleVisibilityAnimated(usernameChangeBox));
        changeEmail.setOnClickListener(v -> toggleVisibilityAnimated(EmailChangeBox));
        notificationSettings.setOnClickListener(v -> toggleVisibilityAnimated(notificationSettingsBox));

        btnChangePasswd.setOnClickListener(v -> {
            if (etPassword.getText().toString().isEmpty() ||
                etNewPassword.getText().toString().isEmpty() ||
                etNewPasswordRepeat.getText().toString().isEmpty()) {
                Toast.makeText(mainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!etNewPassword.getText().toString().equals(etNewPasswordRepeat.getText().toString())) {
                Toast.makeText(mainActivity, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            String dbPassword = null;
            try {
                dbPassword = db.checkPassword(logedindUser.getUid()).get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (etNewPassword.getText().toString().equals(etNewPasswordRepeat.getText().toString())) {
                if (BCrypt.verifyer().verify(etPassword.getText().toString().toCharArray(), dbPassword).verified) {
                    db.changePassword(logedindUser.getUid(), etNewPassword.getText().toString());
                    Toast.makeText(mainActivity, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    toggleVisibilityAnimated(passwdChangeBox);

                    etNewPassword.getText().clear();
                    etPassword.getText().clear();
                    etNewPasswordRepeat.getText().clear();
                    toggleVisibilityAnimated(passwdChangeBox);
                } else {
                    Toast.makeText(mainActivity, "Incorrect password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(mainActivity, "Passwords dont match", Toast.LENGTH_SHORT).show();
            }
        });

        btnChangeUsername.setOnClickListener(v -> {
            new AlertDialog.Builder(mainActivity)
                    .setTitle("Confirm")
                    .setMessage("Are you sure you want to change your username?")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            db.changeUserName(logedindUser.getUid(), etNewUsername.getText().toString());
                            logedindUser.setUserName(etNewUsername.getText().toString());
                            etNewUsername.getText().clear();
                            toggleVisibilityAnimated(usernameChangeBox);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

        });

        btnChangeEmail.setOnClickListener(v -> {
            if (etEmail.getText().toString().isEmpty()){
                Toast.makeText(mainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }else {
                new AlertDialog.Builder(mainActivity)
                        .setTitle("Confirm")
                        .setMessage("Are you sure you want to change your email?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                db.changeEmail(logedindUser.getUid(), etEmail.getText().toString());
                                etEmail.getText().clear();
                                toggleVisibilityAnimated(EmailChangeBox);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            /**
                             * Dismisses the given dialog when an option is clicked.
                             */
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        btnSaveNotificationSettings.setOnClickListener(v -> {
            int minute = timePicker.getMinute();
            int hour = timePicker.getHour();
            if (minute!=logedindUser.getMinute() || hour!=logedindUser.getHour()){
                db.setTime(logedindUser.getUid(), LocalTime.of(hour, minute));
            }
            NotificationScheduler.cancelDailyNotification(mainActivity);
            if (notification.isChecked()) {
                NotificationScheduler.scheduleDailyNotification(mainActivity, hour, minute, "Dont forget to complete your goals");
            }
            toggleVisibilityAnimated(notificationSettingsBox);
        });






    }

    /**
     * Toggles the visibility of a RelativeLayout with an animated scale effect.
     */
    private void toggleVisibilityAnimated(RelativeLayout layout) {
        layout.setPivotY(0f); // Set animation pivot to top

        if (layout.getVisibility() == View.GONE) {
            layout.setScaleY(0f);
            layout.setVisibility(View.VISIBLE);
            layout.animate()
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(250)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        } else {
            layout.animate()
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(250)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> layout.setVisibility(View.GONE))
                    .start();
        }

    }

    /**
     * Sets the database connection for this instance.
     */
    public void setDb(DataBaseConnection db) {
        this.db = db;
    }
}