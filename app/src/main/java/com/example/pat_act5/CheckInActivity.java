package com.example.pat_act5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckInActivity extends AppCompatActivity {

    private static final String TAG = "CheckInActivity";

    private TextInputEditText etReservationId;
    private TextView tvReservationRoom, tvReservationDate, tvReservationTime;
    private LinearLayout layoutReservationDetails;
    private Button btnCheckIn;

    private String userId;
    private String token;
    private String reservationId;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    private String roomName, date, startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "=== CheckInActivity onCreate started ===");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        Log.d(TAG, "=== Layout set successfully ===");

        requestQueue = Volley.newRequestQueue(this);

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        initViews();

        userId = sharedPreferences.getString("userId", "");
        Log.d(TAG, "User ID: " + userId);

        // Add debug for token
        debugTokenStatus();

        setupListeners();

    }

    private void initViews() {
        etReservationId = findViewById(R.id.etReservationId);
        tvReservationRoom = findViewById(R.id.tvReservationRoom);
        tvReservationDate = findViewById(R.id.tvReservationDate);
        tvReservationTime = findViewById(R.id.tvReservationTime);
        layoutReservationDetails = findViewById(R.id.layoutReservationDetails);
        btnCheckIn = findViewById(R.id.btnCheckIn);

        layoutReservationDetails.setVisibility(View.GONE);
        btnCheckIn.setVisibility(View.GONE);

        Log.d(TAG, "Views initialized");
    }

    private void debugTokenStatus() {
        String token = sharedPreferences.getString("token", "");
        String userId = sharedPreferences.getString("userId", "");
        String email = sharedPreferences.getString("email", "");

        Log.d(TAG, "=== TOKEN DEBUG ===");
        Log.d(TAG, "User ID: " + (userId.isEmpty() ? "EMPTY" : userId));
        Log.d(TAG, "Email: " + (email.isEmpty() ? "EMPTY" : email));
        Log.d(TAG, "Token: " + (token.isEmpty() ? "EMPTY" : "EXISTS (length: " + token.length() + ")"));
        Log.d(TAG, "Token preview: " + (token.isEmpty() ? "EMPTY" : token.substring(0, Math.min(20, token.length())) + "..."));
        Log.d(TAG, "==================");
    }
    private void setupListeners() {
        // Gunakan TextWatcher untuk real-time checking
        etReservationId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String inputId = s.toString().trim();
                if (inputId.length() >= 3) { // Min 3 karakter baru fetch
                    Log.d(TAG, "Fetching reservation for ID: " + inputId);
                    fetchReservationDetails(inputId);
                } else {
                    hideReservationDetails();
                }
            }
        });

        // Juga tetap pakai OnFocusChangeListener sebagai backup
        etReservationId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String inputId = etReservationId.getText().toString().trim();
                if (!inputId.isEmpty()) {
                    Log.d(TAG, "Focus lost, fetching reservation for ID: " + inputId);
                    fetchReservationDetails(inputId);
                }
            }
        });

        btnCheckIn.setOnClickListener(v -> {
            Log.d(TAG, "Check-in button clicked");
            if (reservationId != null) {
                performCheckIn();
            } else {
                Toast.makeText(this, "Please enter a valid reservation ID first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideReservationDetails() {
        layoutReservationDetails.setVisibility(View.GONE);
        btnCheckIn.setVisibility(View.GONE);
        reservationId = null;
        Log.d(TAG, "Reservation details hidden");
    }

    private void showTestReservationDetails() {
        // Method untuk testing - hapus jika tidak perlu
        tvReservationRoom.setText("Room: Test Room 101");
        tvReservationDate.setText("Date: June 26, 2025");
        tvReservationTime.setText("Time: 09:00 - 11:00");

        layoutReservationDetails.setVisibility(View.VISIBLE);
        btnCheckIn.setVisibility(View.VISIBLE);
        reservationId = "TEST123";

        Log.d(TAG, "Test reservation details shown");
        Toast.makeText(this, "Test reservation details loaded", Toast.LENGTH_SHORT).show();
    }

    private void fetchReservationDetails(String id) {
        Log.d(TAG, "Fetching reservation details for ID: " + id);

        String token = sharedPreferences.getString("token", "");
        if (token.isEmpty()) {
            Log.e(TAG, "No valid token found. User might need to login again.");
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }
        // Cek apakah ApiConfig.RESERVATION_URL tersedia
        if (ApiConfig.RESERVATION_URL + "/" + id == null || ApiConfig.RESERVATION_URL.isEmpty()) {
            Log.e(TAG, "RESERVATION_URL is null or empty");
            Toast.makeText(this, "API configuration error", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.RESERVATION_URL + "/" + id;
        Log.d(TAG, "API URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d(TAG, "API Response received: " + response.toString());
                    try {
                        reservationId = response.getString("reservation_id");
                        roomName = response.getString("room_name");
                        date = response.getString("date");
                        startTime = response.getString("start_time");
                        endTime = response.getString("end_time");

                        Log.d(TAG, "Parsed data - Room: " + roomName + ", Date: " + date);

                        tvReservationRoom.setText("Room: " + roomName);
                        tvReservationDate.setText("Date: " + DateTimeUtils.formatDateTimeForDisplay(date));
                        tvReservationTime.setText("Time: " + startTime + " - " + endTime);

                        layoutReservationDetails.setVisibility(View.VISIBLE);
                        btnCheckIn.setVisibility(View.VISIBLE);

                        Log.d(TAG, "Reservation details shown successfully");
                        Toast.makeText(this, "Reservation found!", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse reservation details", Toast.LENGTH_SHORT).show();
                        hideReservationDetails();
                    }
                },
                error -> {
                    Log.e(TAG, "Fetch Error: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Error Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Error Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Reservation not found", Toast.LENGTH_LONG).show();
                    hideReservationDetails();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                String authToken = sharedPreferences.getString("token", "");
                Map<String, String> headers = new HashMap<>();

                if (!authToken.isEmpty()) {
                    headers.put("Authorization", "Bearer " + authToken);
                    Log.d(TAG, "Authorization header added successfully");
                } else {
                    Log.e(TAG, "Token is empty when setting headers!");
                }

                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void performCheckIn() {
        Log.d(TAG, "Performing check-in for reservation: " + reservationId);

        if (ApiConfig.CHECKIN_URL == null || ApiConfig.CHECKIN_URL.isEmpty()) {
            Log.e(TAG, "CHECKIN_URL is null or empty");
            Toast.makeText(this, "API configuration error", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.CHECKIN_URL;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("reservation_id", reservationId);
            Log.d(TAG, "Check-in payload: " + jsonBody.toString());
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    Log.d(TAG, "Check-in response: " + response.toString());
                    try {
                        String status = response.getString("status");
                        if ("Occupied".equals(status)) {
                            Toast.makeText(this, "Check-in successful!", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Check-in successful");
                            finish();
                        } else {
                            Toast.makeText(this, "Check-in failed: " + status, Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Check-in failed with status: " + status);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Check-in response parsing error: " + e.getMessage());
                        e.printStackTrace();
                        Toast.makeText(this, "Response parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Check-in Error: " + error.getMessage());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Check-in Error Code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Check-in Error Data: " + new String(error.networkResponse.data));
                    }
                    Toast.makeText(this, "Check-in failed. Please try again.", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                String authToken = sharedPreferences.getString("token", "");
                Map<String, String> headers = new HashMap<>();

                if (!authToken.isEmpty()) {
                    headers.put("Authorization", "Bearer " + authToken);
                }
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
    private void redirectToLogin() {
        // Clear stored credentials
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to login activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}