package com.example.pat_act5;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private Context context;

    public RoomAdapter(List<Room> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        holder.tvRoomName.setText(room.getName());
        holder.tvRoomId.setText("Room ID: " + room.getRoomId());
        holder.tvCapacity.setText("Capacity: " + room.getCapacity() + " people");
        holder.tvFacilities.setText("Facilities: " + room.getFacilitiesString());
        holder.tvStatus.setText("Status: " + room.getStatus());

        // Set status color
        if (room.getStatus().equals("Free")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else if (room.getStatus().equals("Occupied")) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
        }

        // Set click listener for room details or reservation
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RoomsActivity.class);
                intent.putExtra("room_id", room.getRoomId());
                intent.putExtra("room_name", room.getName());
                intent.putExtra("capacity", room.getCapacity());
                intent.putExtra("facilities", room.getFacilitiesString());
                intent.putExtra("status", room.getStatus());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }
    // Method to update a specific room's status

    public void updateRoomList(List<Room> newRoomList) {
        this.roomList.clear();
        this.roomList.addAll(newRoomList);
        notifyDataSetChanged();
    }
    public void updateRoomStatus(String roomId, String newStatus) {
        for (int i = 0; i < roomList.size(); i++) {
            Room room = roomList.get(i);
            if (room.getRoomId().equals(roomId)) {
                room.setStatus(newStatus);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvRoomName, tvRoomId, tvCapacity, tvFacilities, tvStatus;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewRoom);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvRoomId = itemView.findViewById(R.id.tvRoomId);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvFacilities = itemView.findViewById(R.id.tvFacilities);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}