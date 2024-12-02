package com.example.mybookshelf;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class UIMaster extends AppCompatActivity {

    MainActivity mainActivity;
    private int timeSpentReading = 0;
    private TextView timeSpentReadingTextView;

    public void setMain(MainActivity main) {
        this.mainActivity = main;

        if (mainActivity != null && mainActivity.findViewById(R.id.etfTimeSpentReading) != null) {
            timeSpentReadingTextView = mainActivity.findViewById(R.id.etfTimeSpentReading);
        } else {
            Log.e("UIMaster", "MainActivity or required TextView is not properly initialized.");
        }
    }

    public void createBookBox(LinearLayout container, Book book) {
        // Ensure that mainActivity is properly initialized
        if (mainActivity == null) {
            Log.e("UIMaster", "MainActivity is null. Cannot create book box.");
            return;
        }

        if (book == null) {
            Log.w("createBookBox", "Received null book object");
            return;
        }

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
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(TextUtils.isEmpty(book.getName()) ? "Unknown" : book.getName()).append("\n");
        details.append("Author: ").append(TextUtils.isEmpty(book.getAuthor()) ? "Unknown" : book.getAuthor()).append("\n");
        details.append("Pages: ").append(book.getPages() > 0 ? book.getPages() : "Unknown").append("\n");
        details.append("Release Date: ").append(book.getRelease_date() > 0 ? book.getRelease_date() : "Unknown");

        // Update the time spent reading
        timeSpentReading += book.getPages();
        if (timeSpentReadingTextView != null) {
            // Update the UI dynamically
            timeSpentReadingTextView.setText("Time Spent Reading: " + timeSpentReading / 60 + "h " + timeSpentReading % 60 + "min");
        }

        bookDetails.setText(details.toString());
        bookDetails.setTextColor(Color.BLACK);
        bookDetails.setTextSize(16);

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

        // Add the book box to the container
        container.addView(bookBox);
    }

    public void clearUI(LinearLayout container){
        container.removeAllViews();
    }

}
