package com.greenhouse.app;

import okhttp3.*;
import java.io.IOException;

public class ApiClient {

    private static final String BASE_URL = "https://api.karnagio.org";
    private static final String API_KEY  = "greenhouse2024";

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json");

    public interface Callback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void get(String endpoint, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("x-api-key", API_KEY)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        if (responseBody != null) {
                            callback.onSuccess(responseBody.string());
                        } else {
                            callback.onSuccess("");
                        }
                    } else {
                        String errorBodyString = "";
                        if (responseBody != null) {
                            errorBodyString = responseBody.string();
                        }
                        callback.onError("HTTP " + response.code() + ": " + errorBodyString);
                    }
                } catch (IOException e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public static void post(String endpoint, String json, Callback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + endpoint)
                .addHeader("x-api-key", API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }
            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (response.isSuccessful()) {
                        if (responseBody != null) {
                            callback.onSuccess(responseBody.string());
                        } else {
                            callback.onSuccess("");
                        }
                    } else {
                        String errorBodyString = "";
                        if (responseBody != null) {
                            errorBodyString = responseBody.string();
                        }
                        callback.onError("HTTP " + response.code() + ": " + errorBodyString);
                    }
                } catch (IOException e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
}
