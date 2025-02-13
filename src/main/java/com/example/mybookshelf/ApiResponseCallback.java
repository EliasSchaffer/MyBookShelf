package com.example.mybookshelf;

public interface ApiResponseCallback {
    void onSuccess(String response);
    void onFailure(String error);
}
