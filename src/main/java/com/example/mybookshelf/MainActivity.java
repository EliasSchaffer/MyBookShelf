package com.example.mybookshelf;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private  Button loginButton;
    private Button addBookButton;
    private Authenticator auth;
    private ApiRequest api;
    private UIMaster uiMaster;
    private Search search;
    private SearchView searchView;
    private TextView timeSpentReadingTextView;
    private Button goToStarting;
    private User logedindUser;

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
            uiMaster = new UIMaster();
            uiMaster.setMain(this);
            search = new Search(this);

            loginButton.setOnClickListener(v -> handleLogin());
        } catch (Exception e) {
            Log.e("MainActivity", "Error during initialization", e);
            Toast.makeText(this, "An error occurred during initialization", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSearch() {
        setContentView(R.layout.search_activity);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        searchView = findViewById(R.id.searchView);
        goToStarting = findViewById(R.id.btnGoBack);
        goToStarting.setOnClickListener(v -> navigateToStartingPage(this.logedindUser));

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
        logedindUser = user;
        setContentView(R.layout.starting_page);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        List<Book> userBooks = user.getBookList();
        addBookButton = findViewById(R.id.addBook);
        addBookButton.setOnClickListener(v -> handleSearch());



        if (userBooks != null && !userBooks.isEmpty()) {
            for (Book book : userBooks) {
                if (book != null && !TextUtils.isEmpty(book.getName())) {
                    api.getOneBook(book.getName(), new ApiRequest.BookCallback() {
                        @Override
                        public void onBookFetched(List<Book> books) {
                            // Ensure the list is not empty and fetch the first book
                            if (books != null && !books.isEmpty()) {
                                uiMaster.createBookBox(bookContainer, books.get(0), false); // Pass the first book
                                timeSpentReadingTextView = findViewById(R.id.etfTimeSpentReading);
                                uiMaster.updateReadingTime(books.get(0).getPages(), timeSpentReadingTextView);
                            } else {
                                // Handle error or empty list
                                uiMaster.createBookBox(bookContainer, new Book("An Error occurred, please try again", "0", 0, "NA"), false);
                            }
                        }
                    });
                }
            }
        } else {
            Log.w("MainActivity", "User's book list is null or empty.");
        }
    }

    public void saveBookName(Book book){
        Context context = getBaseContext();
        logedindUser.addBook(book, context);

    }

    public void removeBook(Book book){
        Context context = getBaseContext();
        logedindUser.removeBook(book, context);
    }

    public User getUser(){
        return logedindUser;
    }




}
