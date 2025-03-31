package com.example.mybookshelf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import com.github.mikephil.charting.data.BarEntry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private static final String URL = "jdbc:mysql://192.168.20.95:3306/mybookshelfdb?connectTimeout=2000&socketTimeout=2000";
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
            int uid = -1;
            String sql = "SELECT username, password_hash, user_id FROM users WHERE username = ?";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    name = resultSet.getString("username");
                    passwordHash = resultSet.getString("password_hash");
                    uid = resultSet.getInt("user_id");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return (name != null) ? new User(name, passwordHash, uid) : null;
        });
    }



    public void addUser(String username, String email, String password) {
        executorService.execute(() -> {
            String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

            String checkUserSql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            String insertUserSql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
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
                    "    b.book_id, " +
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
                            resultSet.getString("title"),       // name
                            resultSet.getString("release_date"), // release_date
                            resultSet.getInt("pages"),          // pages
                            resultSet.getString("author"),      // author
                            resultSet.getString("cover_url"),   // image_url
                            resultSet.getString("description") , // description
                            resultSet.getInt("book_id")
                    );
                    System.out.println("Retrieved book ID: " + book.getId());
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
        executorService.execute(() ->{

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
        });
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

    public class BookService {

        private Connection connection;

        // Constructor to initialize the database connection
        public BookService(Connection connection) {
            this.connection = connection;
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }


    public Future<ArrayList<BarEntry>> getReadingTimeByMonthAsync(int UID) {
        return executorService.submit(() -> {
            ArrayList<BarEntry> entries = new ArrayList<>();
            String sql = "SELECT MONTH(created_at) AS month, SUM(reading_time) AS total_reading_time " +
                    "FROM userbooks WHERE user_id = ? " +
                    "GROUP BY MONTH(created_at) ORDER BY month";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, UID);
                ResultSet resultSet = preparedStatement.executeQuery();

                // Check if any data is returned
                boolean hasResults = false;

                while (resultSet.next()) {
                    int month = resultSet.getInt("month");
                    int totalTime = resultSet.getInt("total_reading_time");

                    // Log the retrieved data for debugging
                    System.out.println("Month: " + month + ", Total Time: " + totalTime);

                    // Handle NULL values
                    if (resultSet.wasNull()) {
                        totalTime = 0;  // Default to 0 if there's no reading time for this month
                    }

                    // Add the entry to the list
                    entries.add(new BarEntry(month, totalTime));
                    hasResults = true;
                }

                // Check if no data was found
                if (!hasResults) {
                    System.out.println("No results found for UID: " + UID);
                    // Returning dummy entry to indicate no data found
                    ArrayList<BarEntry> temp = new ArrayList<>();
                    temp.add(new BarEntry(99, 1));
                    return temp;
                }

            } catch (SQLException e) {
                e.printStackTrace();
                System.err.println("Error executing query: " + e.getMessage());
            }

            return entries;
        });
    }

    public void removeBookFromUser(Book book, int UID) {
        executorService.execute(() -> {
            // SQL statement to remove the book from the userbooks table
            String sql = "DELETE FROM userbooks WHERE user_id = ? AND book_id = ?";
            String delete = "DELETE FROM NOTES WHERE user_id = ? AND book_id = ?";


            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                try (PreparedStatement deleteStmt = connection.prepareStatement(delete)) {
                    deleteStmt.setInt(1, UID);
                    deleteStmt.setInt(2, book.getId());
                    deleteStmt.executeUpdate();
                }
                // Set the parameters for the query
                preparedStatement.setInt(1, UID);        // Set user_id
                preparedStatement.setInt(2, book.getId()); // Set book_id

                // Execute the delete operation
                int rowsAffected = preparedStatement.executeUpdate();

                // Log the result of the operation
                if (rowsAffected > 0) {
                    System.out.println("Book removed from user's list.");
                } else {
                    System.out.println("No matching record found to remove. BookID" + book.getId() + " UserID " + UID);
                }
            } catch (SQLException e) {
                System.err.println("Error removing book from user: " + e.getMessage());
                e.printStackTrace();  // Print stack trace for better debugging
            }
        });
    }

    public void notesChanged(int UID, int bookID, String note) {
        String delete = "DELETE FROM NOTES WHERE user_id = ? AND book_id = ?";
        String insert = "INSERT INTO Notes (user_id, book_id, note) VALUES (?, ?, ?)";

        executorService.execute(() -> {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                connection.setAutoCommit(false); // Start transaction

                // DELETE existing note
                try (PreparedStatement deleteStmt = connection.prepareStatement(delete)) {
                    deleteStmt.setInt(1, UID);
                    deleteStmt.setInt(2, bookID);
                    deleteStmt.executeUpdate();
                }

                // INSERT new note
                try (PreparedStatement insertStmt = connection.prepareStatement(insert)) {
                    insertStmt.setInt(1, UID);
                    insertStmt.setInt(2, bookID);
                    insertStmt.setString(3, note);
                    insertStmt.executeUpdate();
                }

                connection.commit(); // Commit transaction
            } catch (SQLException e) {
                System.err.println("Error updating book note: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public String getNotesFromUser(int UID, int bookID) {
        String sql = "SELECT note FROM NOTES WHERE user_id = ? AND book_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, UID);
            preparedStatement.setInt(2, bookID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) { // Ensure ResultSet is properly closed
                return resultSet.next() ? resultSet.getString("note") : "Add notes here...";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Add notes here...";
    }





}