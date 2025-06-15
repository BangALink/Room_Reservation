package com.example.pat_act5;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

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

    private static final String BASE_URL = "http://192.168.1.100:4000";

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
        String url = BASE_URL + "/app/api/rooms";
        String token = sharedPreferences.getString("token", "");

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        roomList.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject roomObj = response.getJSONObject(i);

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

                                roomList.add(room);
                            }
                            roomAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RoomsActivity.this, "Error parsing room data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
