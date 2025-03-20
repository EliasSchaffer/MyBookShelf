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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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

    public void setMain(MainActivity main) {
        this.mainActivity = main;
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

        bookBox.setOnClickListener(v -> mainActivity.navigateToDetails(book));


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

    private void setupLineChart() {
        mainActivity.setContentView(R.layout.test_chart);
        barChart = barChart.findViewById(R.id.barChart);
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 10f));  // Entry for Category 1
        entries.add(new BarEntry(1f, 20f));  // Entry for Category 2
        entries.add(new BarEntry(2f, 15f));  // Entry for Category 3
        entries.add(new BarEntry(3f, 25f));  // Entry for Category 4

        BarDataSet dataSet = new BarDataSet(entries, "Sample Data");
        dataSet.setColor(mainActivity.getResources().getColor(android.R.color.holo_blue_light));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);  // Set bar width
        barChart.setData(barData);

        // Customize X-Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Category 1", "Category 2", "Category 3", "Category 4"}));

        barChart.getDescription().setText("Sample Bar Chart");
        barChart.animateY(1000);  // Animation for Y-Axis
        barChart.invalidate();  // Refresh the chart
    }



}
