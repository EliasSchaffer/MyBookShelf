package com.example.mybookshelf;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class User {
    String user;
    String hash_password;
    String email;
    int UID;
    List<Book> bookList= new ArrayList<>();
    DataBaseConnection db = new DataBaseConnection(null);


    public User(String user, String hash_password, int UID) throws ExecutionException, InterruptedException {
        this.user = user;
        this.hash_password = hash_password;
        this.UID = UID;

        /*bookList.add(new Book("Harry Potter und die Kammer des Schreckens"));
        bookList.add(new Book("To Kill a Mockingbird"));
        bookList.add(new Book("1984"));
        bookList.add(new Book("blue box 1"));
        bookList.add(new Book("Erebos"));*/

        bookList = db.getBooksFromUID(UID);
    }

    public User(String user, String hash_password) throws ExecutionException, InterruptedException {
        this.user = user;
        this.hash_password = hash_password;
    }

    public User (String user, String hash_password, String email, int UID){
        this.user = user;
        this.hash_password = hash_password;
        this.email = email;
        this.UID = UID;

        try {
            bookList = db.getBooksFromUID(UID);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        bookList.add(new Book("Harry Potter und die Kammer des Schreckens"));
        bookList.add(new Book("To Kill a Mockingbird"));
        bookList.add(new Book("1984"));
        bookList.add(new Book("blue box 1"));
        bookList.add(new Book("Erebos"));
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return hash_password;
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void addBook(Book book, Context main){
        for (Book listBook:bookList) {
            if (listBook.getName().equals(book.getName())){
                Toast.makeText(main, "Book is already in your List", Toast.LENGTH_SHORT).show();
                Toast.makeText(main,"successfully added book", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        bookList.add(book);

    }

    public void removeBook(Book book, Context main, LinearLayout container) {
        // Remove the book from the list
        Iterator<Book> iterator = bookList.iterator();
        while (iterator.hasNext()) {
            Book listBook = iterator.next();
            if (listBook.getName().equals(book.getName())) {
                iterator.remove(); // Safely remove the book from the list
                break; // Exit the loop after removal
            }
        }

        // Remove the book's visual part from the UI
        for (int i = 0; i < container.getChildCount(); i++) {
            View bookBox = container.getChildAt(i);
            // Assuming you have a way to tag or identify the book's visual part, like setting a tag to the bookBox
            if (bookBox instanceof RelativeLayout) {
                // You might want to set a tag when creating the book box, so you can identify it later
                if (bookBox.getTag() != null && bookBox.getTag().equals(book.getName())) {
                    container.removeViewAt(i); // Remove the visual part
                    break; // Exit the loop after removal
                }
            }
        }

        // Show an error message if the book wasn't found
        Toast.makeText(main, "An Error occurred, please try again later or reload the site", Toast.LENGTH_SHORT).show();
    }


}


