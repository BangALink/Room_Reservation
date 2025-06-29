package com.example.pat_act5;

public class ApiConfig {
    // Replace with your server IP address: http://YOUR_IP_ADDRESS:5000
    public static final String BASE_URL = "http://192.168.1.226:5000";

    // Authentication endpoints
    public static final String LOGIN_URL = BASE_URL + "/app/api/auth/login";
    public static final String REGISTER_URL = BASE_URL + "/app/api/auth/register";

    // Room endpoints
    public static final String ROOMS_URL = BASE_URL + "/app/api/rooms";
    public static final String ROOM_ID_URL = BASE_URL + "/app/api/rooms/:id";

    // Reservation endpoints
    public static final String RESERVATION_URL = BASE_URL + "/app/api/reservation";

    // Check-in endpoint
    public static final String CHECKIN_URL = BASE_URL + "/app/api/checkin";

    // Feedback endpoint
    public static final String FEEDBACK_URL = BASE_URL + "/app/api/feedback";

    // Users endpoint
    public static final String USERS_URL = BASE_URL + "/app/api/users/";
    public static final String USERS_ID_URL = BASE_URL + "/app/api/users/:id";

    // Request timeout
    public static final int TIMEOUT_MS = 15000;
}
