package com.example.pat_act5;

public class Reservation {
    private String reservationId;
    private String userId;
    private String roomId;
    private String roomName;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
    private String createdAt;

    public Reservation() {
    }

    public Reservation(String reservationId, String userId, String roomId, String roomName,
                       String date, String startTime, String endTime, String status) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // Getters and Setters
    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimeRange() {
        return startTime + " - " + endTime;
    }
}
