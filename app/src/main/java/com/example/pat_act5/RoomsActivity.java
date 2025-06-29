package com.example.pat_act5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RoomAdapter roomAdapter;
    private List<Room> roomList;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        initViews();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        setupRecyclerView();
        loadRooms();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRooms(); // Refresh data setiap kali activity muncul kembali
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String updatedRoomId = data.getStringExtra("updated_room_id");
            String newStatus = data.getStringExtra("new_status");

            if (updatedRoomId != null && newStatus != null) {
                updateRoomStatus(updatedRoomId, newStatus);
            }
        }
    }

    public void updateRoomStatus(String roomId, String newStatus) {
        for (Room room : roomList) {
            if (room.getRoomId().equals(roomId)) {
                room.setStatus(newStatus);
                roomAdapter.updateRoomStatus(roomId, newStatus);
                break;
            }
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewRooms);
    }

    private void setupRecyclerView() {
        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(roomList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(roomAdapter);
    }

    private void loadRooms() {
        String url = ApiConfig.ROOMS_URL;
        String token = sharedPreferences.getString("token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("API_RESPONSE", "Raw response: " + response.toString());
                        List<Room> tempRoomList = new ArrayList<>();

                        try {
                            // PERBAIKAN: Cek apakah ada key "rooms" dan ambil arraynya
                            if (response.has("rooms")) {
                                JSONArray roomsArray = response.getJSONArray("rooms");

                                for (int i = 0; i < roomsArray.length(); i++) {
                                    JSONObject roomObj = roomsArray.getJSONObject(i);

                                    Room room = new Room();
                                    room.setRoomId(roomObj.getString("room_id"));
                                    room.setName(roomObj.getString("name"));
                                    room.setCapacity(roomObj.getInt("capacity"));
                                    room.setStatus(roomObj.getString("status"));

                                    // Parse facilities
                                    JSONArray facilitiesArray = roomObj.getJSONArray("facilities");
                                    List<String> facilities = new ArrayList<>();
                                    for (int j = 0; j < facilitiesArray.length(); j++) {
                                        facilities.add(facilitiesArray.getString(j));
                                    }
                                    room.setFacilities(facilities);

                                    tempRoomList.add(room);
                                }
                                roomAdapter.updateRoomList(tempRoomList);
                                roomList.clear();
                                roomList.addAll(tempRoomList);

                            } else {
                                Log.e("JSON_ERROR", "Key 'rooms' not found in response");
                                Toast.makeText(RoomsActivity.this, "Invalid response format", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Error parsing room data: " + e.getMessage());
                            e.printStackTrace();
                            Toast.makeText(RoomsActivity.this, "Error parsing room data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY_ERROR", "Error loading rooms: " + error.toString());
                        Toast.makeText(RoomsActivity.this, "Error loading rooms: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
}
