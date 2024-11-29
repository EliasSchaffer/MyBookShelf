package com.example.mybookshelf;

import java.util.ArrayList;
import java.util.List;

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
}


