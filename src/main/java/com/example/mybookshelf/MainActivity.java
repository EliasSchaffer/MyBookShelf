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
    private Authenticator auth;
    private ApiRequest api;

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
                                Log.w("MainActivity", "Failed to fetch book data for: " + book.getName());
                                // Optionally handle UI error here (e.g., show a placeholder or error message)
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
        String imageUrl = book.getImageUrl();
        if (!imageUrl.isEmpty()) {
            // Load the image asynchronously (you can use Glide or Picasso for better performance)
            Glide.with(this)
                    .load(imageUrl)
                    .into(bookImage);
        } else {
            // Fallback to a default image if no image URL is available
            Log.w("createBookBox", "No image URL available for book: " + book.getName());
        }

        // Set up the layout for the image (aligned to the left of the book details)
        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
                150, 200); // Set your image dimensions here
        imageParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        bookImage.setLayoutParams(imageParams);

        // Create TextView for book details
        TextView bookDetails = new TextView(this);
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(TextUtils.isEmpty(book.getName()) ? "Unknown" : book.getName()).append("\n");
        details.append("Author: ").append(TextUtils.isEmpty(book.getAuthor()) ? "Unknown" : book.getAuthor()).append("\n");
        details.append("Pages: ").append(book.getPages() != -1 ? book.getPages() : "Unknown").append("\n");
        details.append("Release Date: ").append(book.getRelease_date() != -1 ? book.getRelease_date() : "Unknown");

        bookDetails.setText(details.toString());
        bookDetails.setTextColor(Color.BLACK);
        bookDetails.setTextSize(16);

        // Set up layout for the book details (aligned to the right of the image)
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.addRule(RelativeLayout.RIGHT_OF, bookImage.getId());
        bookDetails.setLayoutParams(textParams);

        // Add ImageView and TextView to the RelativeLayout
        bookBox.addView(bookImage);
        bookBox.addView(bookDetails);

        // Add book box to the container
        container.addView(bookBox);
    }


}
