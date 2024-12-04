package com.example.mybookshelf;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;

public class User {
    String user;
    String password;
    List<Book> bookList= new ArrayList<>();


    public User(String user, String password) {
        this.user = user;
        this.password = password;

        bookList.add(new Book("Harry Potter und die Kammer des Schreckens"));
        bookList.add(new Book("To Kill a Mockingbird"));
        bookList.add(new Book("1984"));
        bookList.add(new Book("blue box 1"));
        bookList.add(new Book("kimitte watashi no koto suki nandesho 1"));
        bookList.add(new Book("Niggers in Paris"));
        bookList.add(new Book("Mein Kampf"));
        bookList.add(new Book("Felix der Neger"));
        bookList.add(new Book("Schwänze für Blondine"));

    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void addBook(Book book, Context main){
        for (Book listBook:bookList) {
            if (listBook.getName().equals(book.getName())){
                Toast.makeText(main, "Book is already in your List", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        bookList.add(book);

    }
}


