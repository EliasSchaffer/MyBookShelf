package com.example.mybookshelf;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.example.mybookshelf.R;

import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

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
    private Button addBookButton;
    private TextView timeSpentReadingTextView;
    private BooksAPI booksAPI;
    private AiAPI ai;
    private User logedindUser;
    public void setMain(MainActivity main) {
        this.mainActivity = main;
    }

    public UIMaster(){
        booksAPI = new BooksAPI();
        ai = new AiAPI();
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
        // Ensure that mainActivity is properly initialized
        if (mainActivity == null) {
            Log.e("UIMaster", "MainActivity is null. Cannot create book box.");
            return;
        }

        if (book == null) {
            Log.w("createBookBox", "Received null book object");
            return;
        }

        String name = book.getName();

        // Create a container for the book box
        RelativeLayout bookBox = new RelativeLayout(mainActivity);
        bookBox.setBackgroundColor(Color.WHITE);
        bookBox.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        boxParams.setMargins(16, 16, 16, 16);
        bookBox.setLayoutParams(boxParams);

        // Create an ImageView for the book cover
        ImageView bookImage = new ImageView(mainActivity);
        bookImage.setId(View.generateViewId()); // Set a unique ID for the ImageView
        String imageUrl = book.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(mainActivity)
                    .load(imageUrl)
                    .into(bookImage);
        } else {
            Log.w("createBookBox", "No image URL available for book: " + book.getName());
        }

        // Set up layout for the ImageView
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                150, 200 // Adjust dimensions as needed
        );
        imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        bookImage.setLayoutParams(imageParams);

        // Create a TextView for the book details
        TextView bookDetails = new TextView(mainActivity);
        StringBuilder details;
        details = new StringBuilder();
        details.append("Name: ").append(TextUtils.isEmpty(book.getName()) ? "Unknown" : book.getName()).append("\n");
        details.append("Author: ").append(TextUtils.isEmpty(book.getAuthor()) ? "Unknown" : book.getAuthor()).append("\n");
        details.append("Pages: ").append(book.getPages() > 0 ? book.getPages() : "Unknown").append("\n");
        details.append("Release Date: ").append(book.getRelease_date() != null ? book.getRelease_date() : "Unknown");


        if (isSearch)  {
            Button btnAdd = getBtnAdd(book);
            bookBox.addView(btnAdd);
        }


        //FIX THIS PLEASE
        if (!isSearch && !Objects.equals(book.getName(), "An Error occurred please try again")){
            Button btnRemove = getBtnRemove(new Book(name, book.getPages()));
            bookBox.addView(btnRemove);
        }


        bookDetails.setText(details.toString());
        bookDetails.setTextColor(Color.BLACK);
        bookDetails.setTextSize(16);
        bookBox.setTag(name);

        // Set up layout for the TextView
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.RIGHT_OF, bookImage.getId());
        textParams.addRule(RelativeLayout.CENTER_VERTICAL);
        textParams.setMargins(16, 0, 0, 0); // Add spacing between the image and text
        bookDetails.setLayoutParams(textParams);

        // Add the ImageView and TextView to the RelativeLayout
        bookBox.addView(bookImage);
        bookBox.addView(bookDetails);

        bookBox.setOnClickListener(v -> navigateToDetails(book));


        // Add the book box to the container
        container.addView(bookBox);
    }

    @NonNull
    private Button getBtnAdd(Book book) {
        Button btnAdd = new Button(mainActivity);
        btnAdd.setOnClickListener(v -> mainActivity.saveBookName(book));
        btnAdd.setText("Add Book"); // Use string resource for text

        // Set layout parameters for the button
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnAdd.setLayoutParams(btnParams);
        return btnAdd;
    }

    @NonNull
    private Button getBtnRemove(Book book) {
        Button btnAdd = new Button(mainActivity);
        btnAdd.setOnClickListener(v -> mainActivity.removeBook(book));
        btnAdd.setText("Remove Book"); // Use string resource for text

        // Set layout parameters for the button
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        btnAdd.setLayoutParams(btnParams);
        return btnAdd;
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
        mainActivity.setContentView(R.layout.starting_page);
        LinearLayout bookContainer = mainActivity.findViewById(R.id.bookContainer);
        List<Book> userBooks = logedindUser.getBookList();
        addBookButton = mainActivity.findViewById(R.id.addBook);
        addBookButton.setOnClickListener(v -> mainActivity.handleSearch());

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
        mainActivity.setContentView(R.layout.detail_activity);

        TextView txtDetails = mainActivity.findViewById(R.id.txtDetails);
        StringBuilder str = new StringBuilder();
        str.append(book.getName()).append("\n").append(book.getAuthor()).append(book.getRelease_date());
        txtDetails.setText(str.toString());

        TextView txtDescription = mainActivity.findViewById(R.id.txtDescription);
        txtDescription.setText(book.getDescription());

        ImageView imgCover = mainActivity.findViewById(R.id.imgCover);
        Glide.with(mainActivity).load(book.getImageUrl()).into(imgCover);

        Button submitButton = mainActivity.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            EditText inpInputText = mainActivity.findViewById(R.id.inpInputText);
            String prompt = inpInputText.getText().toString();
            inpInputText.setText(" ");
            ai.fetchResponse(prompt + " The books name is " + book.getName(), mainActivity);
        });

        Button backButton2 = mainActivity.findViewById(R.id.btnGoBack2);
        backButton2.setOnClickListener(v -> {
            try {
                navigateToStartingPage();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
