package com.example.mybookshelf.dataClass;

public class Notification {
    private int id;
    private String message;

    public Notification(int id, String message) {
        this.id = id;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
}
