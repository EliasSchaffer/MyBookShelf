package com.example.mybookshelf;

import java.util.Date;

public class Book {
    private String name;
    private String author;
    private int pages;
    private String release_date;
    private String image_url;
    private String description;
    private boolean inDatabase;
    private int id;

    public Book(String name, String release_date, int pages, String author) {
        this.name = name;
        this.release_date = release_date;
        this.pages = pages;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public boolean isInDatabase() {
        return inDatabase;
    }

    public void setInDatabase(boolean inDatabase) {
        this.inDatabase = inDatabase;
    }

    public Book(String name, String release_date, int pages, String author, String image_url, String description, int id) {
        this.name = name;
        this.release_date = release_date;
        this.pages = pages;
        this.author = author;
        this.image_url = image_url;
        this.description = description;
        this.id = id;
    }

    public Book(String name, String release_date, int pages, String author, String image_url, String description) {
        this.name = name;
        this.release_date = release_date;
        this.pages = pages;
        this.author = author;
        this.image_url = image_url;
        this.description = description;
        this.id = id;
    }

    public Book(String name, int pages) {
        this.name = name;
        this.pages = pages;
    }

    public Book(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getRelease_date() {
        return release_date;
    }

    public int getPages() {
        return pages;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageUrl() {
        return image_url;
    }

    public String getDescription() {
        return description;
    }
}
