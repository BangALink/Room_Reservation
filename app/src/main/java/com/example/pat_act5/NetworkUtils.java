package com.example.pat_act5;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static JsonObjectRequest createAuthenticatedRequest(
            int method,
            String url,
            org.json.JSONObject jsonRequest,
            com.android.volley.Response.Listener<org.json.JSONObject> listener,
            com.android.volley.Response.ErrorListener errorListener,
            Context context) {

        return new JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                String authHeader = SharedPrefManager.getInstance(context).getAuthorizationHeader();
                if (authHeader != null) {
                    headers.put("Authorization", authHeader);
                }

                return headers;
            }
        };
    }

    public static JsonObjectRequest createRequest(
            int method,
            String url,
            org.json.JSONObject jsonRequest,
            com.android.volley.Response.Listener<org.json.JSONObject> listener,
            com.android.volley.Response.ErrorListener errorListener) {

        return new JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
    }

    public static String getErrorMessage(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String errorMessage = new String(error.networkResponse.data);
                org.json.JSONObject errorJson = new org.json.JSONObject(errorMessage);
                return errorJson.optString("message", "Unknown error occurred");
            } catch (Exception e) {
                return "Network error occurred";
            }
        }
        return error.getMessage() != null ? error.getMessage() : "Network error occurred";
    }
}
