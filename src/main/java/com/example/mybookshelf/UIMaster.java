package com.example.mybookshelf;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Objects;

public class UIMaster extends AppCompatActivity {

    MainActivity mainActivity;
    private int timeSpentReading = 0;

    public void setMain(MainActivity main) {
        this.mainActivity = main;
    }

    public void reduceTimeSpendReading(int time, TextView timeSpentReadingTextView){

        timeSpentReading-=(time*1.5);
        if (timeSpentReadingTextView != null) {
            runOnUiThread(() ->
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

    public void clearUI(LinearLayout container){
        container.removeAllViews();
    }

    public void updateReadingTime(int pages, TextView timeSpentReadingTextView){
        timeSpentReading += (pages*1.5);
        if (timeSpentReadingTextView != null) {
            runOnUiThread(() ->
                    timeSpentReadingTextView.setText("Time Spent Reading: " + + (timeSpentReading / (24 * 60)) + " d, " + ((timeSpentReading % (24 * 60)) / 60) + " h " + ((timeSpentReading % (24 * 60)) % 60) + " min")
            );
        }
    }

}
