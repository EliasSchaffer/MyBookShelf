package com.example.mybookshelf;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Objects;

public class Search extends AppCompatActivity {

    ApiRequest api;
    UIMaster uiMaster;
    public Search(MainActivity main) {
        api = new ApiRequest();
        uiMaster = new UIMaster();
        uiMaster.setMain(main);

    }

    public void searchByName(String bookName, LinearLayout bookContainer) {
        api.getMultipleBooks(bookName, new ApiRequest.BookCallback() {
            @Override
            public void onBookFetched(List<Book> books) {
                // Check if books is not null and contains at least one item
                if (books != null && !books.isEmpty()) {
                    // Use the first book from the list
                    for (Book book: books) {
                        uiMaster.createBookBox(bookContainer, book);
                    }

                } else {
                    // If no books found, create a fallback Book
                    uiMaster.createBookBox(bookContainer, new Book("An Error occurred please try again", "0", 0, "NA"));
                }
            }
        });
    }


}
