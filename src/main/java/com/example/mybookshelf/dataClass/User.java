package com.example.mybookshelf.dataClass;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mybookshelf.DataBaseConnection;
import com.example.mybookshelf.MainActivity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class User {
    private String user;
    private String hash_password;
    private String email;
    private int UID;
    private LinkedList<Book> bookList= new LinkedList<>();
    private TreeSet<String> authorList = new TreeSet<>();
    private LinkedList<Goal> goalList = new LinkedList<>();
    private LinkedList<Notification> notificationList = new LinkedList<>();
    private TreeSet<String> genreList;
    private MainActivity mainActivity;
    private DataBaseConnection db;


    public User(String user, String hash_password, int UID, DataBaseConnection db) throws ExecutionException, InterruptedException {
        this.user = user;
        this.hash_password = hash_password;
        this.UID = UID;
        this.db = db;
        genreList = new TreeSet<>();
        authorList = new TreeSet<>();
        db.getAllGoalsForUser(UID, goals -> {
            for (Goal goal : goals) {
                goalList.add(goal);
            }
        });
        db.getAllNotificationsForUser(UID, notifications -> {
            for (Notification notification : notifications) {
                notificationList.add(notification);
            }
        });

        bookList = new LinkedList<>(db.getBooksFromUID(UID).get());
        for (Book book: bookList){
            book.setInDatabase(true);
        }


    }

    public User(String user, String hash_password) throws ExecutionException, InterruptedException {
        this.user = user;
        this.hash_password = hash_password;
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

    public TreeSet<String> getAuthors(){
        for (Book book : bookList) {
            authorList.add(book.getAuthor());
        }
        return authorList;
    }

    public TreeSet<String> getGenres(){
        for (Book book : bookList) {
            String genre = book.getGenre();
            if (genre != null) {
                genreList.add(genre);
            }
        }
        return genreList;
    }


    public int getUid() {
        return UID;
    }

    public LinkedList<Goal> getGoalList() {
        return goalList;
    }
    public long addGoal(Goal goal){
        goalList.add(goal);
        return goal.getId();
    }
}



