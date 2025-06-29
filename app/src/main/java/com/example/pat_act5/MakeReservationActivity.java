package com.example.pat_act5;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MakeReservationActivity extends AppCompatActivity {

    private Spinner spinnerRooms;
    private EditText etDate, etStartTime, etEndTime;
    private Button btnSelectDate, btnSelectStartTime, btnSelectEndTime, btnMakeReservation;

    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;
    private List<Room> roomList;
    private ArrayAdapter<String> roomAdapter;

    private Calendar selectedDate, startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_reservation);

        initViews();
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        selectedDate = Calendar.getInstance();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();

        setupClickListeners();
        loadRooms();
    }

    private void initViews() {
        spinnerRooms = findViewById(R.id.spinnerRooms);
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectStartTime = findViewById(R.id.btnSelectStartTime);
        btnSelectEndTime = findViewById(R.id.btnSelectEndTime);
        btnMakeReservation = findViewById(R.id.btnMakeReservation);

        roomList = new ArrayList<>();
    }

    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        btnSelectStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartTimePicker();
            }
        });

        btnSelectEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndTimePicker();
            }
        });

        btnMakeReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeReservation();
            }
        });
    }

    private void loadRooms() {
        String url = ApiConfig.ROOMS_URL;
        String token = sharedPreferences.getString("token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", "Raw response: " + response.toString());
                        roomList.clear();
                        List<String> roomNames = new ArrayList<>();

                        try {
                            if (response.has("rooms")) {
                                JSONArray roomsArray = response.getJSONArray("rooms");

                                for (int i = 0; i < roomsArray.length(); i++) {
                                    JSONObject roomObj = roomsArray.getJSONObject(i);

                                    Room room = new Room();
                                    room.setRoomId(roomObj.getString("room_id"));
                                    room.setName(roomObj.getString("name"));
                                    room.setCapacity(roomObj.getInt("capacity"));
                                    room.setStatus(roomObj.getString("status"));

                                    roomList.add(room);
                                    roomNames.add(room.getName() + " (ID: " + room.getRoomId() + ")");
                                }

                                // Setup spinner adapter
                                roomAdapter = new ArrayAdapter<>(MakeReservationActivity.this,
                                        android.R.layout.simple_spinner_item, roomNames);
                                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerRooms.setAdapter(roomAdapter);

                            } else {
                                Log.e("JSON_ERROR", "Key 'rooms' not found in response");
                                Toast.makeText(MakeReservationActivity.this, "Invalid response format", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing room data: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(MakeReservationActivity.this, "Error parsing room data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY_ERROR", "Error loading rooms: " + error.toString());
                        Toast.makeText(MakeReservationActivity.this, "Error loading rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        etDate.setText(sdf.format(selectedDate.getTime()));
                    }
                }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showStartTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        startTime.set(Calendar.MINUTE, minute);
                        etStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }
                }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), true);

        timePickerDialog.show();
    }

    private void showEndTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        endTime.set(Calendar.MINUTE, minute);
                        etEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                    }
                }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), true);

        timePickerDialog.show();
    }

    private void makeReservation() {
        if (spinnerRooms.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return;
        }

        String date = etDate.getText().toString().trim();
        String startTimeStr = etStartTime.getText().toString().trim();
        String endTimeStr = etEndTime.getText().toString().trim();

        if (date.isEmpty() || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate time
        if (startTime.getTimeInMillis() >= endTime.getTimeInMillis()) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedRoomIndex = spinnerRooms.getSelectedItemPosition();
        Room selectedRoom = roomList.get(selectedRoomIndex);

        // Debug SharedPreferences - coba beberapa kemungkinan key
        String user_id = sharedPreferences.getString("user_id", "");
        String userId = sharedPreferences.getString("userId", ""); // alternatif key
        String _id = sharedPreferences.getString("_id", ""); // alternatif key lain
        String token = sharedPreferences.getString("token", "");

        // Enhanced logging untuk debugging
        Log.d("RESERVATION_DEBUG", "=== SharedPreferences Debug ===");
        Log.d("RESERVATION_DEBUG", "user_id: " + user_id);
        Log.d("RESERVATION_DEBUG", "userId: " + userId);
        Log.d("RESERVATION_DEBUG", "_id: " + _id);
        Log.d("RESERVATION_DEBUG", "All SharedPreferences keys: " + sharedPreferences.getAll().keySet());
        Log.d("RESERVATION_DEBUG", "Room ID: " + selectedRoom.getRoomId());
        Log.d("RESERVATION_DEBUG", "Date: " + date);
        Log.d("RESERVATION_DEBUG", "Start Time: " + startTimeStr);
        Log.d("RESERVATION_DEBUG", "End Time: " + endTimeStr);
        Log.d("RESERVATION_DEBUG", "Token: " + (token.isEmpty() ? "EMPTY" : "EXISTS"));

        // Pilih user_id yang tidak kosong
        String finalUserId = "";
        if (!user_id.isEmpty()) {
            finalUserId = user_id;
        } else if (!userId.isEmpty()) {
            finalUserId = userId;
        } else if (!_id.isEmpty()) {
            finalUserId = _id;
        }

        Log.d("RESERVATION_DEBUG", "Final User ID: " + finalUserId);

        JSONObject reservationData = new JSONObject();
        try {
            reservationData.put("user_id", finalUserId);
            reservationData.put("room_id", selectedRoom.getRoomId());
            reservationData.put("date", date);
            reservationData.put("start_time", startTimeStr);
            reservationData.put("end_time", endTimeStr);
            Log.d("RESERVATION_DEBUG", "Request Body: " + reservationData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ApiConfig.RESERVATION_URL;
        Log.d("RESERVATION_DEBUG", "Request URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, reservationData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String reservationId = response.getString("reservation_id");

                            Toast.makeText(MakeReservationActivity.this,
                                    "Reservation successful! ID: " + reservationId, Toast.LENGTH_LONG).show();

                            // Clear form
                            clearForm();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MakeReservationActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Reservation failed";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data);
                                JSONObject errorObj = new JSONObject(errorBody);
                                errorMessage = errorObj.getString("message");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(MakeReservationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void clearForm() {
        spinnerRooms.setSelection(0);
        etDate.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
    }
}