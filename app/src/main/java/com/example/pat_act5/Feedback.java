package com.example.pat_act5;

public class Feedback {
    private String userId;
    private String roomId;
    private String reservationId;
    private int rating;
    private String comments;
    private String createdAt;

    public Feedback() {
    }

    public Feedback(String userId, String roomId, int rating, String comments) {
        this.userId = userId;
        this.roomId = roomId;
        this.rating = rating;
        this.comments = comments;
    }

    public Feedback(String userId, String roomId, String reservationId, int rating, String comments) {
        this.userId = userId;
        this.roomId = roomId;
        this.reservationId = reservationId;
        this.rating = rating;
        this.comments = comments;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public int getRating() {
        return rating;
    }

    public String getComments() {
        return comments;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "userId='" + userId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", rating=" + rating +
                ", comments='" + comments + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
