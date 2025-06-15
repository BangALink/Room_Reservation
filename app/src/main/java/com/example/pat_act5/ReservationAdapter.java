package com.example.pat_act5;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservationList;
    private Context context;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    private static final String BASE_URL = "http://192.168.1.100:4000";

    public ReservationAdapter(List<Reservation> reservationList, Context context) {
        this.reservationList = reservationList;
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        holder.tvReservationId.setText("ID: " + reservation.getReservationId());
        holder.tvRoomName.setText(reservation.getRoomName());
        holder.tvDate.setText("Date: " + reservation.getDate());
        holder.tvTime.setText("Time: " + reservation.getTimeRange());
        holder.tvStatus.setText("Status: " + reservation.getStatus());

        // Set status color
        if (reservation.getStatus().equals("Reserved")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
        } else if (reservation.getStatus().equals("Occupied")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else if (reservation.getStatus().equals("Canceled")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }

        // Show/hide buttons based on status
        if (reservation.getStatus().equals("Reserved")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCheckIn.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnCheckIn.setVisibility(View.GONE);
        }

        // Set click listeners
        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog(reservation, position);
            }
        });

        holder.btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn(reservation, position);
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show reservation details
                showReservationDetails(reservation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    private void showCancelDialog(Reservation reservation, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Cancel Reservation")
                .setMessage("Are you sure you want to cancel this reservation?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelReservation(reservation, position);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelReservation(Reservation reservation, int position) {
        String url = BASE_URL + "/app/api/reservation/" + reservation.getReservationId();
        String token = sharedPreferences.getString("token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("canceled")) {
                                reservation.setStatus("Canceled");
                                notifyItemChanged(position);
                                Toast.makeText(context, "Reservation canceled successfully", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Failed to cancel reservation", Toast.LENGTH_SHORT).show();
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

    private void checkIn(Reservation reservation, int position) {
        String userEmail = sharedPreferences.getString("email", "");

        JSONObject checkInData = new JSONObject();
        try {
            checkInData.put("user_id", userEmail);
            checkInData.put("reservation_id", reservation.getReservationId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = BASE_URL + "/app/api/checkin";
        String token = sharedPreferences.getString("token", "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, checkInData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if (status.equals("Occupied")) {
                                reservation.setStatus("Occupied");
                                notifyItemChanged(position);
                                Toast.makeText(context, "Check-in successful", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Check-in failed", Toast.LENGTH_SHORT).show();
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

    private void showReservationDetails(Reservation reservation) {
        String details = "Reservation Details:\n\n" +
                "ID: " + reservation.getReservationId() + "\n" +
                "Room: " + reservation.getRoomName() + "\n" +
                "Date: " + reservation.getDate() + "\n" +
                "Time: " + reservation.getTimeRange() + "\n" +
                "Status: " + reservation.getStatus();

        new AlertDialog.Builder(context)
                .setTitle("Reservation Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvReservationId, tvRoomName, tvDate, tvTime, tvStatus;
        Button btnCancel, btnCheckIn;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewReservation);
            tvReservationId = itemView.findViewById(R.id.tvReservationId);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }
}
