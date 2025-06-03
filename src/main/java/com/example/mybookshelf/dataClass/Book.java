package com.example.mybookshelf.dataClass;

public class Book {
    private String genre;
    private String name;
    private String author;
    private int pages;
    private String release_date;
    private String image_url;
    private String description;
    private boolean inDatabase;
    private int id;
    private int reading_time;
    private String status;

    public Book(String name, String release_date, int pages, String author) {
        this.name = name;
        this.release_date = release_date;
        this.pages = pages;
        this.author = author;
    }

    /**
     * Returns the genre.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Returns the unique identifier of the object.
     */
    public int getId() {
        return id;
    }

    /**
     * Checks if the object is present in the database.
     */
    public boolean isInDatabase() {
        return inDatabase;
    }

    /**
     * Sets whether the object is stored in the database.
     */
    public void setInDatabase(boolean inDatabase) {
        this.inDatabase = inDatabase;
    }

    public Book(String name, String release_date, int pages, String author, String image_url, String description, int id, String genre, String Status) {
        this.name = name;
        this.release_date = release_date;
        this.pages = pages;
        this.author = author;
        this.image_url = image_url;
        this.description = description;
        this.id = id;
        this.genre = genre;
        this.status = Status;
    }

    public Book(String name, String release_date, int pages, String author, String image_url, String description, int id, String genre) {
        this.name = name;
        this.release_date = release_date;
        this.pages = pages;
        this.author = author;
        this.image_url = image_url;
        this.description = description;
        this.id = id;
        this.genre = genre;
    }


    public Book(String name,String release_date , int pages,String author , String image_url, String description, String genre) {
        this.name = name;
        this.release_date = release_date;
        this.pages = pages;
        this.author = author;
        this.image_url = image_url;
        this.description = description;
        this.genre = genre;
    }

    public Book(String name, int pages) {
        this.name = name;
        this.pages = pages;
    }

    public Book(String name) {
        this.name = name;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the release date of the item.
     */
    public String getRelease_date() {
        return release_date;
    }

    /**
     * Returns the number of pages.
     */
    public int getPages() {
        return pages;
    }

    /**
     * Returns the author of the book.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the URL of the image.
     */
    public String getImageUrl() {
        return image_url;
    }

    /**
     * Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the release date of the product.
     */
    public String getReleaseDate() {
        return release_date;
    }

    /**
     * Sets the author of the document.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Sets the release date.
     */
    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }
    /**
     * Sets the status to the specified value.
     */
    public void setStatus(String status) {
        this.status = status;

    }
    /**
     * Returns the current status.
     */
    public String getStatus() {
        return status;

    }
}
