package com.example.pat_act5;

import android.content.Intent;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();

        requestQueue = Volley.newRequestQueue(this);
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        userId = sharedPrefManager.getUserId();

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
        adapter = new ReservationAdapter(reservationList, this);
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
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadMyReservations() {
        showLoading(true);

        // Since the server doesn't have a direct endpoint for user reservations,
        // we'll need to modify this based on your server implementation
        // For now, let's assume we have an endpoint or we fetch all and filter client-side
        String url = ApiConfig.BASE_URL + "/reservations/user/" + userId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        parseReservations(response);
                        showLoading(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error loading reservations: " + error.getMessage());
                        showLoading(false);

                        // If endpoint doesn't exist, show empty state with message
                        String message = NetworkUtils.getErrorMessage(error);
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            showEmptyState("No reservations found");
                        } else {
                            Toast.makeText(MyReservationsActivity.this,
                                    "Error loading reservations: " + message, Toast.LENGTH_SHORT).show();
                            showEmptyState("Failed to load reservations");
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(MyReservationsActivity.this);
                String token = sharedPrefManager.getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void parseReservations(JSONArray jsonArray) {
        reservationList.clear();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Reservation reservation = new Reservation();
                reservation.setReservationId(jsonObject.getString("reservation_id"));
                reservation.setUserId(jsonObject.getString("user_id"));
                reservation.setRoomId(jsonObject.getString("room_id"));
                reservation.setRoomName(jsonObject.optString("room_name", "Unknown Room"));
                reservation.setDate(jsonObject.getString("date"));
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
        loadMyReservations();
    }
}