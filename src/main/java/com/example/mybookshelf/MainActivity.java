package com.example.mybookshelf;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ApiResponseCallback {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button addBookButton;
    private Authenticator auth;
    private BooksAPI booksAPI;
    private AiAPI ai;
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
            booksAPI = new BooksAPI();
            uiMaster = new UIMaster();
            uiMaster.setMain(this);
            search = new Search(this);
            ai = new AiAPI();

            loginButton.setOnClickListener(v -> {
                try {
                    handleLogin();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error during initialization", e);
            Toast.makeText(this, "An error occurred during initialization", Toast.LENGTH_SHORT).show();
        }

//        String question = "What is the plot of 'Moby Dick'?";  // Your desired question
//        AiAPI.fetchResponse(question, MainActivity.this);
    }

    private void handleSearch() {
        setContentView(R.layout.search_activity);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        searchView = findViewById(R.id.searchView);
        goToStarting = findViewById(R.id.btnGoBack);
        goToStarting.setOnClickListener(v -> {
            try {
                navigateToStartingPage(this.logedindUser);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

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


    private void handleLogin() throws ExecutionException, InterruptedException {
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

    private void navigateToStartingPage(User user) throws ExecutionException, InterruptedException {
        logedindUser = user;
        setContentView(R.layout.starting_page);
        LinearLayout bookContainer = findViewById(R.id.bookContainer);
        List<Book> userBooks = user.getBookList();
        addBookButton = findViewById(R.id.addBook);
        addBookButton.setOnClickListener(v -> handleSearch());


        if (userBooks != null && !userBooks.isEmpty()) {
            for (Book book : userBooks) {
                if (book != null && !TextUtils.isEmpty(book.getName())) {
                    booksAPI.getOneBook(book.getName(), new BooksAPI.BookCallback() {
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

    void navigateToDetails(Book book) {
        setContentView(R.layout.detail_activity);

        TextView txtDetails = findViewById(R.id.txtDetails);
        StringBuilder str = new StringBuilder();
        str.append(book.getName()).append("\n").append(book.getAuthor()).append(book.getRelease_date());
        txtDetails.setText(str.toString());

        TextView txtDescription = findViewById(R.id.txtDescription);
        //txtDescription.setText(book.getDescription());

        ImageView imgCover = findViewById(R.id.imgCover);
        Glide.with(this).load(book.getImageUrl()).into(imgCover);

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            EditText inpInputText = findViewById(R.id.inpInputText);
            String prompt = inpInputText.getText().toString();
            inpInputText.setText("");
            //ai.fetchResponse("The Question is:" + prompt + "The book is " + book.getName() + " if the question isnt about books say that that question is not about books and they should ask something else. if you know the book provide a short answer with max 50 words and skip all the following instructions . if you dont know the book say:-1", MainActivity.this);
            ai.fetchResponse(prompt + " The books name is " + book.getName(), MainActivity.this);
        });


    }


    public void saveBookName(Book book) {
        Context context = getBaseContext();
        logedindUser.addBook(book, context);

    }


    public void removeBook(Book book) {
        Context context = getBaseContext();
        uiMaster.reduceTimeSpendReading(book.getPages(), timeSpentReadingTextView);
        logedindUser.removeBook(book, context, findViewById(R.id.bookContainer));
    }

    public User getUser() {
        return logedindUser;
    }


    //AI Response Handeling !!REMOVE LATER!!
    @Override
    public void onSuccess(String response) {
        // Show the response in a simple dialog when the request succeeds
        String result = "";
        // JSON string to parse

        // Define regex to extract the "response" field value (handling newlines)
        String regex = "\"response\":\"(.*?)\"";

        // Create Pattern object with DOTALL mode to capture newlines
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);

        // Create matcher object
        Matcher matcher = pattern.matcher(response);

        // If a match is found, print the response
//        if (matcher.find()) {
//            result = matcher.group(1); // Extracting the matched value
//            System.out.println("Response: ");
//            System.out.println(response.replace("\\n", "\n")); // Replace escaped newlines with actual newlines
//        } else {
//            System.out.println("No response found.");
//        }

        result = response;
        showResponseDialog(result);
    }

    @Override
    public void onFailure(String error) {
        // Show the error in a dialog when the request fails
        showResponseDialog(error);
    }

    private void showResponseDialog(String message) {

        TextView boxReturn = findViewById(R.id.boxReturn);
        // Simple pop-up without any buttons or extra UI elements
        boxReturn.setText("");
        boxReturn.setText(message);
    }
}
