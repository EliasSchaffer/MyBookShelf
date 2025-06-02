package com.example.mybookshelf.dataClass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mybookshelf.DataBaseConnection;
import com.example.mybookshelf.MainActivity;
import com.example.mybookshelf.notifications.NotificationScheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private LinkedList<Goal> completedGoalList = new LinkedList<>();
    private LinkedList<Goal> failedGoalList = new LinkedList<>();

    private LinkedList<Notification> notificationList = new LinkedList<>();
    private TreeSet<String> genreList;
    private MainActivity mainActivity;
    private DataBaseConnection db;
    private boolean reminder;
    private int hour;
    private int minute;


    @SuppressLint("SuspiciousIndentation")
    public User(String user, String hash_password, int UID, DataBaseConnection db, boolean reminder, int hour, int minute, MainActivity mainActivity) throws ExecutionException, InterruptedException {
        this.user = user;
        this.hash_password = hash_password;
        this.UID = UID;
        this.db = db;
        genreList = new TreeSet<>();
        authorList = new TreeSet<>();

        // Initialize the goal lists before using them
        goalList = new LinkedList<>();
        completedGoalList = new LinkedList<>();

        db.getAllGoalsForUser(UID, (List<Goal> goalsList) -> {
            for (Goal goal : goalsList) {

                    Log.i("Goal", goal.toString());
                    if (goal.isCompleted()) {
                        completedGoalList.add(goal);
                    } else if (goal.getDeadline().isBefore(LocalDateTime.now())){
                        failedGoalList.add(goal);
                        NotificationScheduler.cancelOneTimeNotification(mainActivity, goal.getId());

                    } else {
                        if (!NotificationScheduler.isOneTimeNotificationScheduled(mainActivity, goal.getId())){
                            NotificationScheduler.scheduleOneTimeNotification(mainActivity, goal.getDeadline(), goal.getId());
                        }
                        goalList.add(goal);
                    }

            }
        });

        bookList = new LinkedList<>(db.getBooksFromUID(UID).get());
        for (Book book: bookList){
            book.setInDatabase(true);
        }

        if (goalList.isEmpty()){
            NotificationScheduler.cancelDailyNotification(mainActivity);
        }
    }

    @SuppressLint("SuspiciousIndentation")
    public User(String user, int UID, DataBaseConnection db, boolean reminder, int hour, int minute, MainActivity mainActivity) throws ExecutionException, InterruptedException {
        this.user = user;
        this.hash_password = hash_password;
        this.UID = UID;
        this.db = db;
        genreList = new TreeSet<>();
        authorList = new TreeSet<>();

        // Initialize the goal lists before using them
        goalList = new LinkedList<>();
        completedGoalList = new LinkedList<>();

        db.getAllGoalsForUser(UID, (List<Goal> goalsList) -> {
            for (Goal goal : goalsList) {

                Log.i("Goal", goal.toString());
                if (goal.isCompleted()) {
                    completedGoalList.add(goal);
                } else if (goal.getDeadline().isBefore(LocalDateTime.now())){
                    failedGoalList.add(goal);
                    NotificationScheduler.cancelOneTimeNotification(mainActivity, goal.getId());

                } else {
                    if (!NotificationScheduler.isOneTimeNotificationScheduled(mainActivity, goal.getId())){
                        NotificationScheduler.scheduleOneTimeNotification(mainActivity, goal.getDeadline(), goal.getId());
                    }
                    goalList.add(goal);
                }

            }
        });
        // Initialize notification list before using it
        notificationList = new LinkedList<>();

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


    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public boolean isReminder() {
        return reminder;
    }

    public void setReminder(boolean reminder) {
        this.reminder = reminder;
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

    public void removeBook(Book book, Context main, RecyclerView container) {
        boolean bookFound = false;

        // Remove the book from the list
        Iterator<Book> iterator = bookList.iterator();
        while (iterator.hasNext()) {
            Book listBook = iterator.next();
            if (listBook.getName().equals(book.getName())) {
                iterator.remove(); // Safely remove the book from the list
                bookFound = true;
                break; // Exit the loop after removal
            }
        }

        // Remove the book's visual part from the UI if the book was found in the list
        if (bookFound) {
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

            // Show success message
            Toast.makeText(main, "Book successfully removed", Toast.LENGTH_SHORT).show();

            // If the book is in the database, you might want to remove it there too
            if (book.isInDatabase() && db != null) {
                try {
                    db.removeBookFromUser(book, getUid());
                } catch (Exception e) {
                    Log.e("User", "Error removing book from database", e);
                    Toast.makeText(main, "Error syncing with database", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Show an error message only if the book wasn't found
            Toast.makeText(main, "Book not found in your list", Toast.LENGTH_SHORT).show();
        }
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

    public LinkedList<Goal> getCompletedGoalList() {
        return completedGoalList;
    }

    public void setUserName(String username){
        this.user = username;;
    }

    public List<Goal> getFailedGoalList() {
        return failedGoalList;
    }
}



