package com.example.mybookshelf;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BooksAPI {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler to post to main thread

    // Interface for callbacks for single and multiple books
    public interface BookCallback {
        void onBookFetched(List<Book> book);
    }

    public interface BooksCallback {
        void onBooksFetched(List<Book> books);
    }

    // Fetch a single book
    public void getOneBook(String bookName, final BookCallback callback) {
        executorService.execute(() -> {
            try {
                // Fetch books from the API
                List<Book> books = fetchBooks("https://www.googleapis.com/books/v1/volumes?q=intitle:" + bookName + "&maxResults=1");
                // If the list is empty, create a fallback book
                if (books.isEmpty()) {
                    books = new ArrayList<>();
                    books.add(new Book("An Error occurred, please try again", "0", 0, "NA", "", ""));
                }
                List<Book> finalBooks = books;
                mainHandler.post(() -> callback.onBookFetched(finalBooks)); // Pass as a list
            } catch (Exception e) {
                Log.e("ApiRequest", "Error fetching single book", e);
                mainHandler.post(() -> {
                    List<Book> fallback = new ArrayList<>();
                    fallback.add(new Book("An Error occurred, please try again", "0", 0, "NA", "", ""));
                    callback.onBookFetched(fallback);
                });
            }
        });
    }


    // Fetch multiple books
    public void getMultipleBooks(String bookName, final BookCallback callback) {
        executorService.execute(() -> {
            try {
                List<Book> books = fetchBooks("https://www.googleapis.com/books/v1/volumes?q=intitle:" + bookName);
                mainHandler.post(() -> callback.onBookFetched(books));
            } catch (Exception e) {
                Log.e("ApiRequest", "Error fetching multiple books", e);
                mainHandler.post(() -> callback.onBookFetched(null)); // Handle errors gracefully
            }
        });
    }

    // Shared method to fetch books from the API
    private List<Book> fetchBooks(String urlString) {
        List<Book> books = new ArrayList<>();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                String response = new Scanner(reader).useDelimiter("\\A").next();
                reader.close();

                JSONArray items = new JSONObject(response).optJSONArray("items");
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject bookJson = items.getJSONObject(i).getJSONObject("volumeInfo");
                        String title = bookJson.optString("title", "No Title Available");
                        String authors = bookJson.optJSONArray("authors") != null ?
                                bookJson.getJSONArray("authors").join(", ") : "No Authors Available";
                        int pageCount;
                        try {
                             pageCount = bookJson.optInt("pageCount", -1);
                        } catch (NullPointerException npe){
                             pageCount = 0;
                        }
                        String year = (bookJson.optString("publishedDate", "Unknown"));

                        // Get the image URL
                        String imageUrl = bookJson.optJSONObject("imageLinks") != null ?
                                bookJson.getJSONObject("imageLinks").optString("thumbnail", "") : "";

                        String description = bookJson.optString("description", "No Description Available");

                        books.add(new Book(title, year, pageCount, authors, imageUrl, description));
                    }
                }
                reader.close();
            }
        connection.disconnect();
        } catch (IOException | JSONException e) {
            Log.e("ApiRequest", "Error processing the request", e);
        }

        return books;
    }
}
