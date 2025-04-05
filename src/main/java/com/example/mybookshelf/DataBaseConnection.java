package com.example.mybookshelf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

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
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    public CompletableFuture<User> getLogin(String username) {
        CompletableFuture<User> future = new CompletableFuture<>();
        final int QUERY_TIMEOUT_SECONDS = 5;

        executorService.execute(() -> {
            try (Connection connection = DriverManager.getConnection(
                    URL + "&connectTimeout=" + (QUERY_TIMEOUT_SECONDS * 1000) +
                            "&socketTimeout=" + (QUERY_TIMEOUT_SECONDS * 1000),
                    USER, PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement(
                         "SELECT username, password_hash, user_id FROM users WHERE username = ?")) {

                // Set timeout at both JDBC and network level
                stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        future.complete(new User(
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getInt("user_id"),
                                this
                        ));
                    } else {
                        future.complete(null);
                    }
                }
            } catch (SQLTimeoutException e) {
                future.completeExceptionally(new Exception("Query timed out after " + QUERY_TIMEOUT_SECONDS + " seconds"));
            } catch (Exception e) {
                future.completeExceptionally(new Exception("Database error: " + e.getMessage()));
            }
        });

        return future;
    }

    private Connection createConnectionWithTimeout() throws SQLException {
        // MySQL-specific timeout parameters in URL
        return DriverManager.getConnection(URL, USER, PASSWORD);
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
                            resultSet.getString("description"), // description
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

    public Future<Book> getBookByName(String bookName) {
        String sql = "SELECT book_id, title, author, pages, release_date, cover_url, description FROM Books WHERE title = ?;";

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
                            resultSet.getInt("book_id")
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null; // Falls kein Buch gefunden wurde
        });
    }


    public void addBookToUser(int userId, String bookName, String author, int pages, String releaseDate, String imageUrl, String description, int readingTime) {
        String getBookIdSQL = "SELECT book_id FROM Books WHERE title = ?";
        String insertUserBookSQL = "INSERT INTO UserBooks (user_id, book_id, reading_time) VALUES (?, ?, ?)";
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
        String sql = "SELECT note FROM NOTES WHERE user_id = ? AND book_id = ?";

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
}
