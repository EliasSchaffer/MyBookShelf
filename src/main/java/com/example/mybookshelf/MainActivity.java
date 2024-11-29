package com.example.mybookshelf;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView timeSpentReadingTextView;
    private Authenticator auth;
    private ApiRequest api;
    private int timeSpentReading = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        try {
            // Initialize UI components
            usernameEditText = findViewById(R.id.txfUser);
            passwordEditText = findViewById(R.id.txfPassword);
            loginButton = findViewById(R.id.btnLogin);

            // Initialize Authenticator and ApiRequest
            auth = new Authenticator();
            api = new ApiRequest();

            loginButton.setOnClickListener(v -> handleLogin());
        } catch (Exception e) {
            Log.e("MainActivity", "Error during initialization", e);
            Toast.makeText(this, "An error occurred during initialization", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(username, password);
        if (auth.checkLogin(user)) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            navigateToStartingPage(user);
        } else {
            Toast.makeText(this, "User or Password incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToStartingPage(User user) {
        setContentView(R.layout.starting_page);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        timeSpentReadingTextView = findViewById(R.id.etfTimeSpentReading);

        List<Book> userBooks = user.getBookList();

        if (userBooks != null && !userBooks.isEmpty()) {
            for (Book book : userBooks) {
                if (book != null && !TextUtils.isEmpty(book.getName())) {
                    api.getBookByNameAsync(book.getName(), new ApiRequest.BookCallback() {
                        @Override
                        public void onBookFetched(Book book) {
                            if (book != null) {
                                createBookBox(bookContainer, book);
                            } else {
                                createBookBox(bookContainer, new Book("An Error occurred please try again", 0, 0, "NA"));
                            }
                        }
                    });
                }
            }
        } else {
            Log.w("MainActivity", "User's book list is null or empty.");
        }
    }

    private void createBookBox(LinearLayout container, Book book) {
        if (book == null) {
            Log.w("createBookBox", "Received null book object");
            return;
        }

        // Create a container for the book box
        RelativeLayout bookBox = new RelativeLayout(this);
        bookBox.setBackgroundColor(Color.WHITE);
        bookBox.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        boxParams.setMargins(16, 16, 16, 16);
        bookBox.setLayoutParams(boxParams);

        // Create an ImageView for the book cover
        ImageView bookImage = new ImageView(this);
        bookImage.setId(View.generateViewId()); // Set a unique ID for the ImageView
        String imageUrl = book.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
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
        TextView bookDetails = new TextView(this);
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




}
