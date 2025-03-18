package com.example.mybookshelf;

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

public class AiAPI {

    private static final String API_URL = "https://api.cloudflare.com/";
    private static final String API_KEY = "5z2t0nUwiYnyRRf1w_qDB1Eai0K_yuhQg8BAT0nk";  // No "Bearer" prefix

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();

    private interface AiService {
        @POST("client/v4/accounts/b8ef0a87c7a80c0d51c106c1314f906d/ai/run/@cf/meta/llama-3-8b-instruct")
        Call<ResponseBody> fetchBooks(@Body RequestBody payload, @Header("Authorization") String authHeader, @Header("Content-Type") String contentType);
    }

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final AiService aiService = retrofit.create(AiService.class);

    public static void fetchResponse(final String question, final ApiResponseCallback callback) {
        JSONArray messages = new JSONArray();
        try {
            // Construct the messages as before
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a friendly assistant that helps answer questions about books with short answers.");
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);

            messages.put(systemMessage);
            messages.put(userMessage);

            JSONObject json = new JSONObject();
            json.put("messages", messages);

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json.toString());

            System.out.println("Request Payload: " + json.toString());

            // Call the API
            Call<ResponseBody> call = aiService.fetchBooks(body, "Bearer " + API_KEY, "application/json");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            callback.onSuccess(responseBody); // Pass response to callback
                        } catch (IOException e) {
                            callback.onFailure("Error parsing response: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        callback.onFailure("Request failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onFailure("Request failed: " + t.getMessage());
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            callback.onFailure("Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}