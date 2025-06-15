package com.example.pat_act5;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CheckInActivity extends AppCompatActivity {
    private static final String TAG = "CheckInActivity";

    private TextView tvReservationId, tvRoomName, tvDate, tvTime;
    private Button btnCheckIn, btnCancel;

    private String reservationId;
    private String userId;
    private String roomName;
    private String date;
    private String startTime;
    private String endTime;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        initViews();
        setupData();
        setupClickListeners();

        requestQueue = Volley.newRequestQueue(this);
    }

    private void initViews() {
        tvReservationId = findViewById(R.id.tv_reservation_id);
        tvRoomName = findViewById(R.id.tv_room_name);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);
        btnCheckIn = findViewById(R.id.btn_check_in);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupData() {
        Intent intent = getIntent();
        reservationId = intent.getStringExtra("reservation_id");
        roomName = intent.getStringExtra("room_name");
        date = intent.getStringExtra("date");
        startTime = intent.getStringExtra("start_time");
        endTime = intent.getStringExtra("end_time");

        // Get user ID from SharedPreferences
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        userId = sharedPrefManager.getUserId();

        // Display reservation details
        tvReservationId.setText("ID: " + reservationId);
        tvRoomName.setText(roomName);
        tvDate.setText(DateTimeUtils.formatDate(date));
        tvTime.setText(startTime + " - " + endTime);
    }

    private void setupClickListeners() {
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performCheckIn();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performCheckIn() {
        String url = ApiConfig.BASE_URL + "/checkin";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("reservation_id", reservationId);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if ("Occupied".equals(status)) {
                                Toast.makeText(CheckInActivity.this,
                                        "Check-in successful! Room is now occupied.",
                                        Toast.LENGTH_LONG).show();

                                // Return to previous activity with success result
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("check_in_success", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parse Error: " + e.getMessage());
                            Toast.makeText(CheckInActivity.this,
                                    "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Check-in Error: " + error.getMessage());
                        String message = NetworkUtils.getErrorMessage(error);
                        Toast.makeText(CheckInActivity.this,
                                "Check-in failed: " + message, Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(CheckInActivity.this);
                String token = sharedPrefManager.getToken();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}
