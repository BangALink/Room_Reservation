package com.example.pat_act5;

import java.util.List;

public class Room {
    private String roomId;
    private String name;
    private int capacity;
    private List<String> facilities;
    private String status;

    public Room() {
    }

    public Room(String roomId, String name, int capacity, List<String> facilities, String status) {
        this.roomId = roomId;
        this.name = name;
        this.capacity = capacity;
        this.facilities = facilities;
        this.status = status;
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFacilitiesString() {
        if (facilities == null || facilities.isEmpty()) {
            return "No facilities";
        }
        return String.join(", ", facilities);
    }
}
