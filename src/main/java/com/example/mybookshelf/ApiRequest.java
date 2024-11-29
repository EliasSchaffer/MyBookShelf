package com.example.mybookshelf;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiRequest {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler to post to main thread

    // Interface for the callback to be used when book data is fetched
    public interface BookCallback {
        void onBookFetched(Book book);
    }

    // Asynchronous method to fetch book details by name with a callback
    public void getBookByNameAsync(String bookName, final BookCallback callback) {
        executorService.execute(() -> {
            try {
                Book book = getBookByName(bookName);
                mainHandler.post(() -> {
                    callback.onBookFetched(book); // Send the fetched book back to the UI
                });
            } catch (Exception e) {
                Log.e("ApiRequest", "Error fetching book details", e);
                mainHandler.post(() -> callback.onBookFetched(null)); // Send null if an error occurred
            }
        });
    }

    // Method to perform the actual API request and parse the response
    public Book getBookByName(String bookName) {
        try {
            bookName = URLEncoder.encode(bookName, "UTF-8");
            String urlString = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + bookName;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                String response = new Scanner(reader).useDelimiter("\\A").next();
                reader.close();

                JSONArray items = new JSONObject(response).optJSONArray("items");
                if (items != null && items.length() > 0) {
                    JSONObject bookJson = items.getJSONObject(0).getJSONObject("volumeInfo");
                    String title = bookJson.optString("title", "No Title Available");
                    String authors = bookJson.optJSONArray("authors") != null ?
                            bookJson.getJSONArray("authors").join(", ") : "No Authors Available";
                    int pageCount = bookJson.optInt("pageCount", -1);
                    int year = parseYear(bookJson.optString("publishedDate", "Unknown"));

                    // Get the image URL
                    String imageUrl = bookJson.optJSONObject("imageLinks") != null ?
                            bookJson.getJSONObject("imageLinks").optString("thumbnail", "") : "";

                    return new Book(title, year, pageCount, authors, imageUrl);
                }
            }
        } catch (IOException | JSONException e) {
            Log.e("ApiRequest", "Error while processing the request", e);
        }
        return null;
    }


    // Helper method to parse the year from the published date
    private int parseYear(String publishedDate) {
        try {
            return Integer.parseInt(publishedDate.substring(0, 4));
        } catch (Exception e) {
            Log.e("ApiRequest", "Error parsing year from date: " + publishedDate, e);
        }
        return -1; // Return -1 if parsing fails
    }
}
