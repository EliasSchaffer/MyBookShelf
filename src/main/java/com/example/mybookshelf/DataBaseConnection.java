package com.example.mybookshelf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class DataBaseConnection {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String URL = "jdbc:mysql://192.168.60.95:3306/mybookshelfdb";
    private static final String USER = "root";
    private static final String PASSWORD = "MYSQLPW1310&";
    private final Context context;

    public DataBaseConnection(Context mainActivity) {
        this.context = mainActivity;
    }

    public Future<User> getLogin(String username) {
        return executorService.submit(() -> {
            String name = null;
            String passwordHash = null;
            String sql = "SELECT username, password_hash FROM users WHERE username = ?";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    name = resultSet.getString("username");
                    passwordHash = resultSet.getString("password_hash");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return (name != null) ? new User(name, passwordHash) : null;
        });
    }



    public void addUser(String username, String email, String password) {
        executorService.execute(() -> {
            // Hash the password using new BCrypt version
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            String insertUserSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // Post the UI update to the main thread


                try (PreparedStatement checkStmt = connection.prepareStatement(checkUserSql)) {
                    checkStmt.setString(1, username);
                    checkStmt.setString(2, email);
                    ResultSet resultSet = checkStmt.executeQuery();

                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        System.out.println("Username or email already exists.");
                        return;
                    }
                }

                try (PreparedStatement insertStmt = connection.prepareStatement(insertUserSql)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, hashedPassword);
                    insertStmt.setString(3, email);

                    int rowsInserted = insertStmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("User added successfully!");
                        ((Activity) context).runOnUiThread(() -> {
                            new AlertDialog.Builder(context)
                                    .setTitle("Debug Info")
                                    .setMessage("user added to db")
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .show();
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }



    public List<Book> getBooksFromUID(int uid) throws ExecutionException, InterruptedException {
        return executorService.submit(() -> {
            List<Book> books = new ArrayList<>();

            String sql = "SELECT " +
                    "    b.title, " +
                    "    b.author, " +
                    "    b.pages, " +
                    "    b.release_date, " +
                    "    b.cover_url, " +
                    "    b.description " +
                    "FROM UserBooks ub " +
                    "JOIN Books b ON ub.book_id = b.book_id " +
                    "WHERE ub.user_id = ?;";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, uid);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Book book = new Book(
                            resultSet.getString("title"),
                            resultSet.getString("author"),
                            resultSet.getInt("pages"),
                            resultSet.getString("release_date"),
                            resultSet.getString("cover_url"),
                            resultSet.getString("description")
                    );
                    books.add(book);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return books;
        }).get();  // Waits for the async result and returns the list
    }

    public Book getBookByName(String bookName) {
        String sql = "SELECT title, author, pages, release_date, cover_url, description FROM Books WHERE title = ?;";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, bookName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new Book(
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("pages"),
                        resultSet.getString("release_date"),
                        resultSet.getString("cover_url"),
                        resultSet.getString("description")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // Return null if no book is found
    }

    public void addBookToUser(int userId, String bookName, String author, int pages, String releaseDate, String imageUrl, String description, int readingTime) {
        String getBookIdSQL = "SELECT book_id FROM Books WHERE title = ?";
        String insertUserBookSQL = "INSERT INTO UserBooks (user_id, book_id, reading_time) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement getBookIdStmt = connection.prepareStatement(getBookIdSQL);
             PreparedStatement insertUserBookStmt = connection.prepareStatement(insertUserBookSQL)) {

            // Get book ID by book name
            getBookIdStmt.setString(1, bookName);
            ResultSet resultSet = getBookIdStmt.executeQuery();

            int bookId;
            if (resultSet.next()) {
                bookId = resultSet.getInt("book_id"); // Book exists, get its ID
            } else {
                // If book is not found, add it to the database and get its ID
                bookId = addNewBook(bookName, author, pages, releaseDate, imageUrl, description);
            }

            // Insert into UserBooks
            insertUserBookStmt.setInt(1, userId);
            insertUserBookStmt.setInt(2, bookId);
            insertUserBookStmt.setInt(3, readingTime);
            insertUserBookStmt.executeUpdate();

            System.out.println("Book successfully added to user!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to add a new book if it doesn't exist
    private int addNewBook(String bookName, String author, int pages, String releaseDate, String imageUrl, String description) {
        String insertBookSQL = "INSERT INTO Books (title, author, pages, release_date, cover_url, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement insertBookStmt = connection.prepareStatement(insertBookSQL, Statement.RETURN_GENERATED_KEYS)) {

            insertBookStmt.setString(1, bookName);
            insertBookStmt.setString(2, author);
            insertBookStmt.setInt(3, pages);
            insertBookStmt.setString(4, releaseDate);
            insertBookStmt.setString(5, imageUrl);
            insertBookStmt.setString(6, description);
            insertBookStmt.executeUpdate();

            // Retrieve the generated book ID
            ResultSet generatedKeys = insertBookStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int bookId = generatedKeys.getInt(1);
                System.out.println("New book added with ID: " + bookId);
                return bookId;
            } else {
                throw new Exception("Failed to retrieve new book ID.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Return an invalid ID if insertion fails
        }
    }




    public void shutdown() {
        executorService.shutdown();
    }


}
