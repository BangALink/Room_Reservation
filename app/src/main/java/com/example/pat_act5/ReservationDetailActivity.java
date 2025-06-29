package com.example.pat_act5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ReservationDetailActivity extends AppCompatActivity {
    private static final String TAG = "ReservationDetail";
    private static final int REQUEST_CHECK_IN = 100;
    private static final int REQUEST_FEEDBACK = 200;

    // Views based on XML layout
    private Toolbar toolbar;
    private ImageView ivStatusIcon;
    private TextView tvStatus, tvReservationId, tvRoomName, tvDate, tvTime, tvCreatedDate;
    private Button btnCancel, btnCheckIn;
    private LinearLayout layoutActions;
    private ProgressBar progressBar;

    // Data variables
    private String reservationId;
    private String userId;
    private String roomId;
    private String roomName;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
    private String createdAt;

    private RequestQueue requestQueue;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_detail);

        initViews();
        setupToolbar();
        setupData();
        setupButtons();

        requestQueue = Volley.newRequestQueue(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatus = findViewById(R.id.tvStatus);
        tvReservationId = findViewById(R.id.tvReservationId);
        tvRoomName = findViewById(R.id.tvRoomName);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);

        btnCancel = findViewById(R.id.btnCancel);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        layoutActions = findViewById(R.id.layoutActions);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupData() {
        Intent intent = getIntent();
        reservationId = intent.getStringExtra("reservation_id");
        userId = intent.getStringExtra("user_id");
        roomId = intent.getStringExtra("room_id");
        roomName = intent.getStringExtra("room_name");
        date = intent.getStringExtra("date");
        startTime = intent.getStringExtra("start_time");
        endTime = intent.getStringExtra("end_time");
        status = intent.getStringExtra("status");
        createdAt = intent.getStringExtra("created_at");

        // Display reservation details
        tvReservationId.setText("ID: " + reservationId);
        tvRoomName.setText(roomName);

        // Format and display date
        if (date != null && !date.isEmpty()) {
            tvDate.setText(DateTimeUtils.formatDateForDisplay(date));
        }

        // Display time range
        if (startTime != null && endTime != null) {
            tvTime.setText(startTime + " - " + endTime);
        }

        // Display created date
        if (createdAt != null && !createdAt.isEmpty()) {
            tvCreatedDate.setText(DateTimeUtils.formatDateTimeForDisplay(createdAt));
        }

        // Update status display
        updateStatusDisplay(status);
    }

    private void updateStatusDisplay(String currentStatus) {
        this.status = currentStatus;
        tvStatus.setText(currentStatus);

        // Set status color and icon based on status
        int colorResId;
        int iconResId;

        switch (currentStatus) {
            case "Reserved":
                colorResId = R.color.success_color;
                iconResId = R.drawable.ic_check_circle;
                break;
            case "Occupied":
                colorResId = R.color.warning_color;
                iconResId = R.drawable.ic_access_time;
                break;
            case "Completed":
                colorResId = R.color.success_color;
                iconResId = R.drawable.ic_check_circle;
                break;
            case "Canceled":
                colorResId = R.color.error_color;
                iconResId = R.drawable.ic_cancel;
                break;
            default:
                colorResId = R.color.black;
                iconResId = R.drawable.ic_info;
                break;
        }

        tvStatus.setTextColor(getResources().getColor(colorResId));
        ivStatusIcon.setImageResource(iconResId);
        ivStatusIcon.setColorFilter(getResources().getColor(colorResId));

        setupButtons();
    }

    private void setupButtons() {
        // Hide all buttons first
        btnCancel.setVisibility(View.GONE);
        btnCheckIn.setVisibility(View.GONE);

        switch (status) {
            case "Reserved":
                // Show cancel button
                btnCancel.setVisibility(View.VISIBLE);
                // Show check-in button if it's time to check in
                if (isReservationTimeNow()) {
                    btnCheckIn.setVisibility(View.VISIBLE);
                }
                break;
            case "Occupied":
                // No action buttons for occupied status
                break;
            case "Completed":
                // No action buttons for completed status
                break;
            case "Canceled":
                // No action buttons for canceled status
                break;
        }

        // Hide action layout if no buttons are visible
        boolean hasVisibleButtons = btnCancel.getVisibility() == View.VISIBLE ||
                btnCheckIn.getVisibility() == View.VISIBLE;
        layoutActions.setVisibility(hasVisibleButtons ? View.VISIBLE : View.GONE);

        setupClickListeners();
    }

    private boolean isReservationTimeNow() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            Date reservationDate = dateFormat.parse(date);
            Date currentDate = new Date();

            // Check if it's the same day
            if (dateFormat.format(reservationDate).equals(dateFormat.format(currentDate))) {
                String currentTime = timeFormat.format(currentDate);
                // Allow check-in 15 minutes before start time
                return isTimeInRange(currentTime, subtractMinutes(startTime, 15), endTime);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time: " + e.getMessage());
        }
        return false;
    }

    private String subtractMinutes(String time, int minutes) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date timeDate = timeFormat.parse(time);
            long timeInMillis = timeDate.getTime() - (minutes * 60 * 1000);
            return timeFormat.format(new Date(timeInMillis));
        } catch (ParseException e) {
            return time;
        }
    }

    private boolean isTimeInRange(String currentTime, String startTime, String endTime) {
        return currentTime.compareTo(startTime) >= 0 && currentTime.compareTo(endTime) <= 0;
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelConfirmation();
            }
        });

        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheckIn();
            }
        });
    }

    private void showCancelConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Reservation");
        builder.setMessage("Are you sure you want to cancel this reservation?");
        builder.setPositiveButton("Yes, Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelReservation();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void cancelReservation() {
        showLoading(true);

        String url = ApiConfig.BASE_URL + "cancel_reservation.php";

        Map<String, String> params = new HashMap<>();
        params.put("reservation_id", reservationId);
        params.put("user_id", sharedPrefManager.getUser().getId());

        JSONObject jsonParams = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        showLoading(false);
                        try {
                            boolean success = response.getBoolean("success");
                            String message = response.getString("message");

                            if (success) {
                                Toast.makeText(ReservationDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                                updateStatusDisplay("Canceled");

                                // Set result to refresh previous activity
                                setResult(RESULT_OK);
                            } else {
                                Toast.makeText(ReservationDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                            Toast.makeText(ReservationDetailActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showLoading(false);
                        Log.e(TAG, "Cancel reservation error: " + error.getMessage());
                        Toast.makeText(ReservationDetailActivity.this, "Failed to cancel reservation", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(request);
    }

    private void startCheckIn() {
        Intent intent = new Intent(this, CheckInActivity.class);
        intent.putExtra("reservation_id", reservationId);
        intent.putExtra("room_id", roomId);
        intent.putExtra("room_name", roomName);
        intent.putExtra("date", date);
        intent.putExtra("start_time", startTime);
        intent.putExtra("end_time", endTime);
        startActivityForResult(intent, REQUEST_CHECK_IN);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCancel.setEnabled(!show);
        btnCheckIn.setEnabled(!show);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CHECK_IN && resultCode == RESULT_OK) {
            // Update status to occupied after successful check-in
            updateStatusDisplay("Occupied");
            setResult(RESULT_OK);
            Toast.makeText(this, "Successfully checked in!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_FEEDBACK && resultCode == RESULT_OK) {
            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchLatestReservationStatus();
    }

    private void fetchLatestReservationStatus() {
        String url = ApiConfig.BASE_URL + "get_reservation_detail.php?id=" + reservationId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String newStatus = response.getString("status");
                            updateStatusDisplay(newStatus);
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed to parse status", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching reservation status", error);
                    }
                });

        requestQueue.add(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}
