package com.example.mybookshelf;

public interface ApiResponseCallback {
    /**
     * Handles a successful operation with the given response.
     */
    void onSuccess(String response);
    /**
     * Handles failure with an error message.
     */
    void onFailure(String error);
}
