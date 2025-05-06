package com.example.mybookshelf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.example.mybookshelf.dataClass.Book;
import com.example.mybookshelf.dataClass.Goal;
import com.example.mybookshelf.dataClass.Notification;
import com.example.mybookshelf.dataClass.User;
import com.github.mikephil.charting.data.BarEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class DataBaseConnection {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    //private static final String URL = "jdbc:mysql://mysql-2ca78de3-mybookshelfdb.c.aivencloud.com:20545/mybookshelf?sslmode=require";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_g46tQJXWz_2Fh_QBWiM";
    private String URL;
    private final Context context;

    private File trustStoreFile; // Store the file reference

    public DataBaseConnection(Context context) {
        this.context = context;
        initialize();
    }

    private void initialize() {
        executorService.execute(() -> {
            try {
                // 1. Load and keep truststore in memory
                KeyStore keyStore = loadTrustStore();

                // 2. Store the truststore file permanently in app's files dir
                trustStoreFile = new File(context.getFilesDir(), "truststore.p12");
                try (OutputStream out = new FileOutputStream(trustStoreFile)) {
                    keyStore.store(out, "changeit".toCharArray());
                }

                // 3. Build URL with permanent truststore path
                this.URL = "jdbc:mysql://mysql-2ca78de3-mybookshelfdb.c.aivencloud.com:20545/mybookshelf" +
                        "?verifyServerCertificate=true" +
                        "&useSSL=true" +
                        "&requireSSL=true" +
                        "&trustCertificateKeyStoreUrl=file:" + trustStoreFile.getAbsolutePath() +
                        "&trustCertificateKeyStorePassword=changeit" +
                        "&trustCertificateKeyStoreType=PKCS12" +
                        "&connectTimeout=5000" +
                        "&socketTimeout=5000";

                // 4. Test connection
                Class.forName("com.mysql.jdbc.Driver");
                try (Connection testConn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                    Log.d("DB", "Datenbankverbindung erfolgreich hergestellt!");
                }
            } catch (Exception e) {
                Log.e("DB", "Initialization error", e);
            }
        });
    }

    private KeyStore loadTrustStore() throws Exception {
        try (InputStream is = context.getAssets().open("truststore.p12")) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(is, "changeit".toCharArray());
            return ks;
        }
    }

    public Future<Integer> checkLogin(String username, char[] password) {
        return executorService.submit(() -> {
            String loginQuery = "SELECT username, password_hash, user_id FROM users WHERE username = ?";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(loginQuery)) {

                stmt.setQueryTimeout(5); // 5 seconds timeout
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // Extract user data from the result set
                        String passwordHash = rs.getString("password_hash");
                        int userId = rs.getInt("user_id");

                        // Verify the password
                        BCrypt.Result result = BCrypt.verifyer().verify(password, passwordHash.toCharArray());
                        if (result.verified) {
                            // Create and return the User object with all details if login is successful
                            connection.close();
                            return userId;
                        }
                    }
                }
            } catch (SQLTimeoutException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Return null if login failed
            return -1;
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


    public Future<List<Book>> getBooksFromUID(int uid) {
        return executorService.submit(() -> {
            List<Book> books = new ArrayList<>();

            String sql = "SELECT " +
                    "b.book_id, " +
                    "b.title, " +
                    "b.author, " +
                    "b.pages, " +
                    "b.release_date, " +
                    "b.cover_url, " +
                    "b.description, " +
                    "GROUP_CONCAT(g.genre_name) AS genre " +  // Changed to singular
                    "FROM userbooks ub " +
                    "LEFT JOIN books b ON ub.book_id = b.book_id " +
                    "LEFT JOIN bookgenre bg ON b.book_id = bg.book_id " +
                    "LEFT JOIN genres g ON bg.genre_id = g.genre_id " +
                    "WHERE ub.user_id = ? " +
                    "GROUP BY b.book_id;";


            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setQueryTimeout(5);  // Set a timeout to prevent hanging
                preparedStatement.setInt(1, uid);

                // Debug: log the query and the parameter being used
                System.out.println("Executing SQL: " + preparedStatement);
                System.out.println("Using UID: " + uid);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        System.out.println("No books found for user ID: " + uid);
                    } else {
                        // Process the results
                        do {
                            Book book = new Book(
                                    resultSet.getString("title"),
                                    resultSet.getString("release_date"),
                                    resultSet.getInt("pages"),
                                    resultSet.getString("author"),
                                    resultSet.getString("cover_url"),
                                    resultSet.getString("description"),
                                    resultSet.getInt("book_id"),
                                    resultSet.getString("genre")
                            );
                            books.add(book);
                        } while (resultSet.next());  // Ensure we process all results
                    }
                }

            } catch (SQLTimeoutException e) {
                System.out.println("SQL query timed out.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }

            // Return the list of books after processing
            return books;
        });
    }


    public Future<Book> getBookByName(String bookName) {
        String sql = "SELECT \n" +
                "    b.book_id,\n" +
                "    b.title,\n" +
                "    b.author,\n" +
                "    b.pages,\n" +
                "    b.release_date,\n" +
                "    b.cover_url,\n" +
                "    b.description,\n" +
                "    GROUP_CONCAT(g.genre_name) AS genre\n" +
                "FROM books b\n" +
                "LEFT JOIN bookgenre bg ON b.book_id = bg.book_id\n" +
                "LEFT JOIN genres g ON bg.genre_id = g.genre_id\n" +
                "WHERE b.title = ?\n" +
                "GROUP BY b.book_id;";


        return executorService.submit(() -> {
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
                            resultSet.getString("description"),
                            resultSet.getInt("book_id"),
                            resultSet.getString("genre")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null; // Falls kein Buch gefunden wurde
        });
    }


    public void addBookToUser(int userId, String bookName, String author, int pages, String releaseDate, String imageUrl, String description, int readingTime, String genre) {
        String getBookIdSQL = "SELECT book_id FROM books WHERE title = ?";
        String insertUserBookSQL = "INSERT INTO userbooks (user_id, book_id, reading_time) VALUES (?, ?, ?)";
        executorService.execute(() -> {

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
                    bookId = addNewBook(bookName, author, pages, releaseDate, imageUrl, description, genre);
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
    private int addNewBook(String bookName, String author, int pages, String releaseDate, String imageUrl, String description, String genre) {
        String insertBookSQL = "INSERT INTO books (title, author, pages, release_date, cover_url, description) VALUES (?, ?, ?, ?, ?, ?)";
        String findGenreSQL = "SELECT genre_id FROM genres WHERE genre_name = ?";
        String insertGenreSQL = "INSERT INTO genres (genre_name) VALUES (?)";
        String insertBookGenreSQL = "INSERT INTO bookgenre (book_id, genre_id) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            connection.setAutoCommit(false); // Start transaction

            int genreId;

            // Check if genre exists
            try (PreparedStatement findGenreStmt = connection.prepareStatement(findGenreSQL)) {
                findGenreStmt.setString(1, genre);
                try (ResultSet rs = findGenreStmt.executeQuery()) {
                    if (rs.next()) {
                        genreId = rs.getInt("genre_id");
                    } else {
                        // Insert new genre
                        try (PreparedStatement insertGenreStmt = connection.prepareStatement(insertGenreSQL, Statement.RETURN_GENERATED_KEYS)) {
                            insertGenreStmt.setString(1, genre);
                            insertGenreStmt.executeUpdate();

                            try (ResultSet generatedGenreKeys = insertGenreStmt.getGeneratedKeys()) {
                                if (generatedGenreKeys.next()) {
                                    genreId = generatedGenreKeys.getInt(1);
                                } else {
                                    throw new Exception("Failed to retrieve genre ID.");
                                }
                            }
                        }
                    }
                }
            }

            int bookId;

            // Insert book
            try (PreparedStatement insertBookStmt = connection.prepareStatement(insertBookSQL, Statement.RETURN_GENERATED_KEYS)) {
                insertBookStmt.setString(1, bookName);
                insertBookStmt.setString(2, author);
                insertBookStmt.setInt(3, pages);
                insertBookStmt.setString(4, releaseDate);
                insertBookStmt.setString(5, imageUrl);
                insertBookStmt.setString(6, description);
                insertBookStmt.executeUpdate();

                try (ResultSet generatedBookKeys = insertBookStmt.getGeneratedKeys()) {
                    if (generatedBookKeys.next()) {
                        bookId = generatedBookKeys.getInt(1);
                    } else {
                        throw new Exception("Failed to retrieve book ID.");
                    }
                }
            }

            // Link book to genre
            try (PreparedStatement insertBookGenreStmt = connection.prepareStatement(insertBookGenreSQL)) {
                insertBookGenreStmt.setInt(1, bookId);
                insertBookGenreStmt.setInt(2, genreId);
                insertBookGenreStmt.executeUpdate();
            }

            connection.commit(); // Commit transaction
            System.out.println("New book and genre link added. Book ID: " + bookId);
            return bookId;

        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Return invalid ID if something goes wrong
        }
    }


    public Future<Float> getRatingFromBook(Book book) {
        String sql = "SELECT AVG(rating) FROM ratings WHERE book_id = ?;";
        return executorService.submit(() -> {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                // Set the parameter for the book_id
                preparedStatement.setInt(1, book.getId());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        // Return the average rating if available
                        return resultSet.getFloat(1);
                    }
                    // Return 0.0f if no result found (book does not exist or has no rating)
                    return 0.0f;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
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
            String delete = "DELETE FROM notes WHERE user_id = ? AND book_id = ?";


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
        String delete = "DELETE FROM notes WHERE user_id = ? AND book_id = ?";
        String insert = "INSERT INTO notes (user_id, book_id, note) VALUES (?, ?, ?)";
        String checkBookExistence = "SELECT COUNT(*) FROM books WHERE book_id = ?";

        executorService.execute(() -> {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                connection.setAutoCommit(false); // Start transaction

                // Check if the book exists in the 'books' table
                try (PreparedStatement checkStmt = connection.prepareStatement(checkBookExistence)) {
                    checkStmt.setInt(1, bookID);
                    ResultSet rs = checkStmt.executeQuery();

                    // If the book doesn't exist, wait for insertion or retry
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.err.println("Error: The book with ID " + bookID + " does not exist yet. Retrying...");
                        // Optionally, you can add retry logic or sleep here to wait for book insertion
                        connection.commit(); // Commit before exiting
                        return;
                    }
                }

                // If book exists or is inserted, proceed to delete and insert notes
                try (PreparedStatement deleteStmt = connection.prepareStatement(delete)) {
                    deleteStmt.setInt(1, UID);
                    deleteStmt.setInt(2, bookID);
                    deleteStmt.executeUpdate();
                }

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


    public Future<String> getNotesFromUser(int UID, int bookID) {
        String sql = "SELECT note FROM notes WHERE user_id = ? AND book_id = ?";

        return executorService.submit(() -> {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, UID);
                preparedStatement.setInt(2, bookID);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("note");
                    }
                    return "Add notes here...";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error retrieving note"; // Ensure return in case of an exception
            }
        });
    }

    public void addNotification(User user, String message) {
        executorService.execute(() -> {
            String sql = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, user.getUid());
                preparedStatement.setString(2, message);
                preparedStatement.setBoolean(3, false);

                int rowsInserted = preparedStatement.executeUpdate();

                if (rowsInserted > 0) {
                    System.out.println("Notification added successfully.");
                } else {
                    System.out.println("No notification was added.");
                }
            } catch (SQLException e) {
                System.err.println("Error adding notification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void removeNotification(int notificationId, int userId) {
        executorService.execute(() -> {
            String sql = "DELETE FROM notifications WHERE notification_id = ? AND user_id = ?";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, notificationId);
                preparedStatement.setInt(2, userId);

                int rowsDeleted = preparedStatement.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println("Notification removed successfully.");
                } else {
                    System.out.println("No matching notification found to remove.");
                }
            } catch (SQLException e) {
                System.err.println("Error removing notification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void getAllNotificationsForUser(int userId, Consumer<List<Notification>> callback) {
        executorService.execute(() -> {
            String sql = "SELECT notification_id, user_id, message, is_read, created_at FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
            List<Notification> notifications = new ArrayList<>();

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Notification notification = new Notification(resultSet.getInt("notification_id"), resultSet.getString("message"));
                        notifications.add(notification);
                    }
                }

                // Pass the list to the callback (e.g., update UI)
                callback.accept(notifications);

            } catch (SQLException e) {
                System.err.println("Error fetching notifications: " + e.getMessage());
                e.printStackTrace();
                callback.accept(Collections.emptyList());
            }
        });
    }

    public long addGoal(Goal goal, User user) {
        // Create an atomic reference to hold the generated ID that can be updated from another thread
        AtomicLong generatedId = new AtomicLong(-1);

        // Create a CountDownLatch to wait for the database operation to complete
        CountDownLatch latch = new CountDownLatch(1);

        // Map verbose goal type to enum value
        String goalTypeEnum;
        switch (goal.getGoalType()) {
            case "Read Books":
                goalTypeEnum = "books";
                break;
            case "Read Pages":
                goalTypeEnum = "pages";
                break;
            case "Read Time":
                goalTypeEnum = "time";
                break;
            case "Read Specific Book":
                goalTypeEnum = "finishBook";
                break;
            default:
                goalTypeEnum = "books"; // fallback or throw an exception
                Log.w("GoalsDebug", "Unknown goal type: " + goal.getGoalType() + ", defaulting to 'books'");
                break;
        }


        executorService.execute(() -> {
            String sql = "INSERT INTO goals (user_id, book_name, target, type, goalType, progress) VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 // Use RETURN_GENERATED_KEYS to get the auto-generated ID
                 PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                preparedStatement.setInt(1, user.getUid());

                // Handle different goal types
                if (goal.getGoalType().equals("Read Specific Book")) {
                    preparedStatement.setString(2, goal.getBookName());
                    preparedStatement.setInt(3, 0); // No target number for book goals
                } else {
                    preparedStatement.setString(2, ""); // No book name
                    preparedStatement.setInt(3, goal.getTarget());
                }

                preparedStatement.setString(4, goal.getGoalType());
                preparedStatement.setString(5,goalTypeEnum);
                preparedStatement.setInt(6, goal.getProgress());

                int rowsInserted = preparedStatement.executeUpdate();

                if (rowsInserted > 0) {
                    // Get the generated ID
                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        generatedId.set(id);
                        System.out.println("Goal added successfully with ID: " + id);

                        // Log success for debugging
                        Log.d("GoalsDebug", "Goal added to database with ID: " + id);
                    }
                } else {
                    System.out.println("No goal was added.");
                    Log.e("GoalsDebug", "No goal was added to database");
                }
            } catch (SQLException e) {
                System.err.println("Error adding goal: " + e.getMessage());
                e.printStackTrace();
                Log.e("GoalsDebug", "Database error: " + e.getMessage(), e);
            } finally {
                // Count down the latch to signal that the operation is complete
                latch.countDown();
            }
        });

        try {
            // Wait for the database operation to complete (with a timeout)
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                Log.e("GoalsDebug", "Database operation timed out");
                return -1;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e("GoalsDebug", "Database operation was interrupted", e);
            return -1;
        }

        // Return the generated ID
        return generatedId.get();
    }

    public void removeGoal(int goalId, int userId) {
        executorService.execute(() -> {
            String sql = "DELETE FROM goals WHERE goal_id = ? AND user_id = ?";

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, goalId);
                preparedStatement.setInt(2, userId);

                int rowsDeleted = preparedStatement.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println("Goal removed successfully.");
                } else {
                    System.out.println("No matching goal found to remove.");
                }
            } catch (SQLException e) {
                System.err.println("Error removing goal: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void getAllGoalsForUser(int userId, Consumer<List<Goal>> callback) {
        executorService.execute(() -> {
            String sql = "SELECT goal_id, user_id, target, progress, created_at FROM goals WHERE user_id = ? ORDER BY created_at DESC";
            List<Goal> goals = new ArrayList<>();

            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, userId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        //TODO change to new Constructor
                        //Goal goal = new Goal(resultSet.getInt("goal_id"), resultSet.getInt("progress"), resultSet.getInt("target"));
                        //goals.add(goal);
                    }
                }
                callback.accept(goals);
            } catch (SQLException e) {
                System.err.println("Error fetching goals: " + e.getMessage());
                e.printStackTrace();
                callback.accept(Collections.emptyList());
            }
        });
    }
}