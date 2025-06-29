package com.example.pat_act5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyReservationsActivity extends AppCompatActivity implements ReservationAdapter.OnReservationClickListener {
    private static final String TAG = "MyReservationsActivity";
    private static final int REQUEST_RESERVATION_DETAIL = 100;

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Reservation> reservationList;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private String userId;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();

        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Get user ID and token using the same method as MakeReservationActivity
        String user_id = sharedPreferences.getString("user_id", "");
        String userIdAlt = sharedPreferences.getString("userId", ""); // alternatif key
        String _id = sharedPreferences.getString("_id", ""); // alternatif key lain
        token = sharedPreferences.getString("token", "");

        // Enhanced logging untuk debugging
        Log.d(TAG, "=== SharedPreferences Debug ===");
        Log.d(TAG, "user_id: " + user_id);
        Log.d(TAG, "userId: " + userIdAlt);
        Log.d(TAG, "_id: " + _id);
        Log.d(TAG, "All SharedPreferences keys: " + sharedPreferences.getAll().keySet());
        Log.d(TAG, "Token: " + (token.isEmpty() ? "EMPTY" : "EXISTS"));

        // Pilih user_id yang tidak kosong
        if (!user_id.isEmpty()) {
            userId = user_id;
        } else if (!userIdAlt.isEmpty()) {
            userId = userIdAlt;
        } else if (!_id.isEmpty()) {
            userId = _id;
        }

        Log.d(TAG, "Final User ID: " + userId);

        // Check if user is authenticated
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            // Redirect to login activity
            finish();
            return;
        }

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMyReservations();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view_reservations);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
    }

    private void setupRecyclerView() {
        reservationList = new ArrayList<>();
        adapter = new ReservationAdapter(reservationList, this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMyReservations();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
    }

    private void loadMyReservations() {
        showLoading(true);

        // Since the server doesn't have a direct endpoint for user reservations,
        // we'll need to modify this based on your server implementation
        // For now, let's assume we have an endpoint or we fetch all and filter client-side
        String url = ApiConfig.RESERVATION_URL + "?user_id=" + userId;

        Log.d(TAG, "Request URL: " + url);
        Log.d(TAG, "Using User ID: " + userId);
        Log.d(TAG, "Using Token: " + (token.isEmpty() ? "EMPTY" : "EXISTS"));

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG, "Response received: " + response.toString());

                            if (response.has("reservations")) {
                                JSONArray reservationsArray = response.getJSONArray("reservations");
                                parseReservations(reservationsArray);
                            } else {
                                Log.e(TAG, "No reservations field in response");
                                showEmptyState("No reservations found");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parse Error: " + e.getMessage());
                            showEmptyState("Error parsing reservation data");
                        }
                        showLoading(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error loading reservations: " + error.toString());
                        showLoading(false);

                        String message = "Failed to load reservations";

                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data);
                                JSONObject errorObj = new JSONObject(errorBody);
                                message = errorObj.getString("message");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            Log.e(TAG, "HTTP Status Code: " + statusCode);

                            if (statusCode == 401 || statusCode == 403) {
                                Toast.makeText(MyReservationsActivity.this,
                                        "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
                                // Clear stored credentials and redirect to login
                                sharedPreferences.edit().clear().apply();
                                finish();
                                return;
                            } else if (statusCode == 404) {
                                showEmptyState("No reservations found");
                                return;
                            }
                        }

                        Toast.makeText(MyReservationsActivity.this,
                                "Error loading reservations: " + message, Toast.LENGTH_SHORT).show();
                        showEmptyState("Failed to load reservations");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                Log.d(TAG, "Request headers: Authorization=Bearer " + (token.isEmpty() ? "EMPTY" : "***"));
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void parseReservations(JSONArray jsonArray) {
        reservationList.clear();

        try {
            Log.d(TAG, "Parsing " + jsonArray.length() + " reservations");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Reservation reservation = new Reservation();
                reservation.setReservationId(jsonObject.getString("reservation_id"));
                reservation.setUserId(jsonObject.getString("user_id"));
                reservation.setRoomId(jsonObject.getString("room_id"));
                reservation.setRoomName(jsonObject.optString("room_name", "Unknown Room"));

                // Handle date parsing - server sends ISO date
                String dateStr = jsonObject.getString("date");
                if (dateStr.contains("T")) {
                    dateStr = dateStr.split("T")[0]; // Extract date part from ISO string
                }
                reservation.setDate(dateStr);

                reservation.setStartTime(jsonObject.getString("start_time"));
                reservation.setEndTime(jsonObject.getString("end_time"));
                reservation.setStatus(jsonObject.getString("status"));
                reservation.setCreatedAt(jsonObject.optString("created_at", ""));

                reservationList.add(reservation);
            }

            if (reservationList.isEmpty()) {
                showEmptyState("You have no reservations yet");
            } else {
                adapter.notifyDataSetChanged();
                showEmptyState(null);
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSON Parse Error: " + e.getMessage());
            Toast.makeText(this, "Error parsing reservation data", Toast.LENGTH_SHORT).show();
            showEmptyState("Error loading reservations");
        }
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(show);
        } else {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(String message) {
        if (message != null) {
            tvEmptyState.setText(message);
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onReservationClick(Reservation reservation) {
        Intent intent = new Intent(this, ReservationDetailActivity.class);
        intent.putExtra("reservation_id", reservation.getReservationId());
        intent.putExtra("user_id", reservation.getUserId());
        intent.putExtra("room_id", reservation.getRoomId());
        intent.putExtra("room_name", reservation.getRoomName());
        intent.putExtra("date", reservation.getDate());
        intent.putExtra("start_time", reservation.getStartTime());
        intent.putExtra("end_time", reservation.getEndTime());
        intent.putExtra("status", reservation.getStatus());
        intent.putExtra("created_at", reservation.getCreatedAt());

        startActivityForResult(intent, REQUEST_RESERVATION_DETAIL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RESERVATION_DETAIL && resultCode == RESULT_OK) {
            // Reservation was modified, reload the list
            loadMyReservations();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh reservations when returning to this activity
        if (token != null && !token.isEmpty()) {
            loadMyReservations();
        }
    }
}