package com.example.pat_act5;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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

public class FeedbackActivity extends AppCompatActivity {
    private static final String TAG = "FeedbackActivity";

    private TextView tvRoomName, tvReservationInfo;
    private RatingBar ratingBar;
    private EditText etComments;
    private Button btnSubmitFeedback, btnCancel;

    private String roomId;
    private String roomName;
    private String reservationId;
    private String userId;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        initViews();
        setupData();
        setupClickListeners();

        requestQueue = Volley.newRequestQueue(this);
    }

    private void initViews() {
        tvRoomName = findViewById(R.id.tv_room_name);
        tvReservationInfo = findViewById(R.id.tv_reservation_info);
        ratingBar = findViewById(R.id.rating_bar);
        etComments = findViewById(R.id.et_comments);
        btnSubmitFeedback = findViewById(R.id.btn_submit_feedback);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupData() {
        Intent intent = getIntent();
        roomId = intent.getStringExtra("room_id");
        roomName = intent.getStringExtra("room_name");
        reservationId = intent.getStringExtra("reservation_id");
        String date = intent.getStringExtra("date");
        String startTime = intent.getStringExtra("start_time");
        String endTime = intent.getStringExtra("end_time");

        // Get user ID from SharedPreferences
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        userId = sharedPrefManager.getUserId();

        // Display room and reservation info
        tvRoomName.setText(roomName);
        String reservationInfo = "Date: " + DateTimeUtils.formatDateForDisplay(date) +
                "\nTime: " + startTime + " - " + endTime;
        if (reservationId != null && !reservationId.isEmpty()) {
            reservationInfo += "\nReservation ID: " + reservationId;
        }
        tvReservationInfo.setText(reservationInfo);

        // Set default rating
        ratingBar.setRating(5.0f);
    }

    private void setupClickListeners() {
        btnSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser && rating < 1.0f) {
                    ratingBar.setRating(1.0f);
                }
            }
        });
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String comments = etComments.getText().toString().trim();

        if (rating < 1.0f) {
            Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.FEEDBACK_URL;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", userId);
            jsonBody.put("room_id", roomId);
            jsonBody.put("rating", (int) rating);
            jsonBody.put("comments", comments);

            if (reservationId != null && !reservationId.isEmpty()) {
                jsonBody.put("reservation_id", reservationId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Disable button to prevent multiple submissions
        btnSubmitFeedback.setEnabled(false);
        btnSubmitFeedback.setText("Submitting...");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if ("success".equals(status)) {
                                Toast.makeText(FeedbackActivity.this,
                                        "Feedback submitted successfully! Thank you for your feedback.",
                                        Toast.LENGTH_LONG).show();

                                // Return to previous activity with success result
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("feedback_submitted", true);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parse Error: " + e.getMessage());
                            Toast.makeText(FeedbackActivity.this,
                                    "Error processing response", Toast.LENGTH_SHORT).show();
                            resetSubmitButton();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Feedback Error: " + error.getMessage());
                        String message = NetworkUtils.getErrorMessage(error);
                        Toast.makeText(FeedbackActivity.this,
                                "Failed to submit feedback: " + message, Toast.LENGTH_LONG).show();
                        resetSubmitButton();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(FeedbackActivity.this);
                String token = sharedPrefManager.getToken();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void resetSubmitButton() {
        btnSubmitFeedback.setEnabled(true);
        btnSubmitFeedback.setText("Submit Feedback");
    }
}