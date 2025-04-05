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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.github.mikephil.charting.formatter.ValueFormatter;

import org.w3c.dom.Text;

public class UIMaster {

    MainActivity mainActivity;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private Button loginButton;
    private Button registerButton;
    private Button switchToLoginButton;
    private Button switchToRegisterButton;

    private int timeSpentReading = 0;
    private ImageButton nav_searchBtn;
    private TextView timeSpentReadingTextView;
    private BooksAPI booksAPI;
    private AiAPI ai;
    private DataBaseConnection db;
    private User logedindUser;

    public UIMaster(MainActivity main){
        booksAPI = new BooksAPI();
        ai = new AiAPI();
        mainActivity = main;
        db = new DataBaseConnection(mainActivity);
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

        // Create a TextView for the book details
        TextView bookDetails = new TextView(mainActivity);
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(TextUtils.isEmpty(book.getName()) ? "Unknown" : book.getName()).append("\n");
        details.append("Author: ").append(TextUtils.isEmpty(book.getAuthor()) ? "Unknown" : book.getAuthor()).append("\n");
        details.append("Pages: ").append(book.getPages() > 0 ? book.getPages() : "Unknown").append("\n");
        details.append("Release Date: ").append(book.getRelease_date() != null ? book.getRelease_date() : "Unknown");

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
            EditText noteField = new EditText(mainActivity);
            String note;
            try {
                note = db.getNotesFromUser(logedindUser.getUid(), book.getId()).get();
            } catch (ExecutionException | InterruptedException e) {
                note = "Add notes here...";
            }
            if (note.equals("Add notes here...") || note.isEmpty()) {
                noteField.setHint(note);
            } else {
                noteField.setText(note);
            }

            noteField.setTextColor(Color.BLACK);
            noteField.setBackgroundColor(Color.LTGRAY);
            noteField.setPadding(8, 8, 8, 8);
            noteField.setTextSize(14);
            noteField.setId(View.generateViewId());

            // Set up layout parameters for the note field
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.setMargins(12, 12, 12, 12);
            noteField.setLayoutParams(noteParams);

            // Add a listener to handle text changes
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

            // Add the note field to the vertical container
            verticalContainer.addView(noteField);
        }

        // Add the vertical container (with all content) inside the book box
        bookBox.addView(verticalContainer);

        // Finally, add the book box to the main container
        container.addView(bookBox);
        container.setOnClickListener(v -> {
            navigateToDetails(book);
        });
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
        mainActivity.setContentView(R.layout.login_activity);
        usernameEditText = mainActivity.findViewById(R.id.txfUser);
        passwordEditText = mainActivity.findViewById(R.id.txfPassword);
        loginButton = mainActivity.findViewById(R.id.btnRegister);
        switchToRegisterButton = mainActivity.findViewById(R.id.btnSwitchToLogin);


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
        mainActivity.setContentView(R.layout.register_activity);
        usernameEditText = mainActivity.findViewById(R.id.txfUser);
        passwordEditText = mainActivity.findViewById(R.id.txfPassword);
        emailEditText = mainActivity.findViewById(R.id.txfEmail);
        registerButton = mainActivity.findViewById(R.id.btnRegister);
        switchToLoginButton = mainActivity.findViewById(R.id.btnSwitchToLogin);


        registerButton.setOnClickListener(v -> {
            try {
                mainActivity.handleRegister(usernameEditText, passwordEditText, emailEditText);
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
        DataBaseConnection db = new DataBaseConnection(mainActivity);
        barChart = mainActivity.findViewById(R.id.barChart);

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

    public void navigateToStartingPage() throws ExecutionException, InterruptedException {
        mainActivity.setContentView(R.layout.main_home);
        LinearLayout bookContainer = mainActivity.findViewById(R.id.bookContainer);
        List<Book> userBooks = logedindUser.getBookList();
        nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        nav_searchBtn.setOnClickListener(v -> mainActivity.handleSearch());
        TextView user = mainActivity.findViewById(R.id.current_user);
        user.setText("Hallo, " + logedindUser.getUser() + " \uD83D\uDC4B");

        if (userBooks != null && !userBooks.isEmpty()) {
            for (Book book : userBooks) {
                if (book != null && !TextUtils.isEmpty(book.getName())) {
                    if (book.isInDatabase()) { // Check if the book exists in the database
                        createBookBox(bookContainer, book, false); // Create the box instantly
                        timeSpentReadingTextView = mainActivity.findViewById(R.id.etfTimeSpentReading);
                        updateReadingTime(book.getPages(), timeSpentReadingTextView);
                    } else {
                        // Fetch the book from API if not in database
                        booksAPI.getOneBook(book.getName(), new BooksAPI.BookCallback() {
                            @Override
                            public void onBookFetched(List<Book> books) {
                                if (books != null && !books.isEmpty()) {
                                    mainActivity.saveBook(books.get(0));
                                    createBookBox(bookContainer, books.get(0), false);
                                    timeSpentReadingTextView = mainActivity.findViewById(R.id.etfTimeSpentReading);
                                    updateReadingTime(books.get(0).getPages(), timeSpentReadingTextView);
                                } else {
                                    createBookBox(bookContainer, new Book("An Error occurred, please try again", "0", 0, "NA"), false);
                                }
                            }
                        });
                    }
                }
            }
        } else {
            Log.w("MainActivity", "User's book list is null or empty.");
        }
    }


    void navigateToDetails(Book book) {
        mainActivity.setContentView(R.layout.main_detail);

        ImageButton nav_homeBtn = mainActivity.findViewById(R.id.nav_home);
        ImageButton nav_searchBtn = mainActivity.findViewById(R.id.nav_search);
        TextView txtAutor = mainActivity.findViewById(R.id.txtAuthor);
        TextView txtTitle = mainActivity.findViewById(R.id.txtTitle);
        TextView txtDescription = mainActivity.findViewById(R.id.txtDescription);
        TextView txtPages = mainActivity.findViewById(R.id.txtPageCount);
        RatingBar rbRating = mainActivity.findViewById(R.id.rbRating);
        ImageView imgCover = mainActivity.findViewById(R.id.imgCover);

        txtAutor.setText(book.getAuthor());
        txtTitle.setText(book.getName());
        txtDescription.setText(book.getDescription());
        txtPages.setText(String.valueOf(book.getPages()));
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
            try {
                navigateToStartingPage();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        Glide.with(mainActivity).load(book.getImageUrl()).into(imgCover);

//        Button submitButton = mainActivity.findViewById(R.id.submitButton);
//        submitButton.setOnClickListener(v -> {
//            EditText inpInputText = mainActivity.findViewById(R.id.inpInputText);
//            String prompt = inpInputText.getText().toString();
//            inpInputText.setText(" ");
//            ai.fetchResponse(prompt + " The books name is " + book.getName(), mainActivity);
//        });

    }
}