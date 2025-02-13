package com.example.mybookshelf;

import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import android.os.Handler;
import android.os.Looper;

public class Search extends AppCompatActivity {

    BooksAPI api;
    UIMaster uiMaster;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public Search(MainActivity main) {
        api = new BooksAPI();
        uiMaster = new UIMaster();
        uiMaster.setMain(main);

    }

    public void searchByName(String bookName, LinearLayout bookContainer) {
        executorService.execute(() -> {
            api.getMultipleBooks(bookName, new BooksAPI.BookCallback() {
                @Override
                public void onBookFetched(List<Book> books) {
                    // Post the results back to the main thread
                    mainThreadHandler.post(() -> {
                        if (books != null && !books.isEmpty()) {
                            for (Book book : books) {
                                uiMaster.createBookBox(bookContainer, book, true);
                            }
                        } else {
                            uiMaster.createBookBox(bookContainer, new Book("An Error occurred please try again", "0", 0, "NA"), false);
                        }
                    });
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
