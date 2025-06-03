package com.example.mybookshelf.dataClass;

public class Notification {
    private int id;
    private String message;

    public Notification(int id, String message) {
        this.id = id;
        this.message = message;
    }

    /**
     * Returns the ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the stored message.
     */
    public String getMessage() {
        return message;
    }
}
