package com.example.mybookshelf;

import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import android.os.Handler;
import android.os.Looper;

import com.example.mybookshelf.apis.BooksAPI;
import com.example.mybookshelf.dataClass.Book;

public class Search extends AppCompatActivity {

    BooksAPI api;
    UIMaster uiMaster;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public Search(MainActivity main) {
        api = new BooksAPI();
        uiMaster = new UIMaster(main);
    }

    /**
     * Searches for books by name and displays them in the specified container.
     */
    public void searchByName(String bookName, LinearLayout bookContainer) {
        executorService.execute(() -> {
            api.getMultipleBooks(bookName, new BooksAPI.BookCallback() {
                @Override
                /**
                 * Updates the UI with fetched books or an error message if no books are available.
                 */
                public void onBookFetched(List<Book> books) {
                    // Post the results back to the main thread
                    mainThreadHandler.post(() -> {
                        if (books != null && !books.isEmpty()) {
                            for (Book book : books) {
                                uiMaster.createBookBox(bookContainer, book, true);
                            }
                        } else {
                            uiMaster.createBookBox(bookContainer, new Book("An Error occurred please try again", "0", 0, "NA", "NA", "NA", "NA"), false);
                        }
                    });
                }
            });
        });
    }

    @Override
    /**
     * Shuts down the executor service if it is not already shut down.
     */
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
