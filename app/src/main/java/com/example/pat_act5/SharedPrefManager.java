package com.example.pat_act5;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "RoomReservationPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_DEPARTMENT = "user_department";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private SharedPrefManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUser(String token, String userId, String email, String name, String role, String department) {
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_DEPARTMENT, department);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        Log.d("SharedPrefManager", "Getting token: " + (token != null ? "EXISTS" : "NULL"));
        return token;
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }

    public String getUserDepartment() {
        return sharedPreferences.getString(KEY_USER_DEPARTMENT, null);
    }

    public User getUser() {
        return new User(
                getUserId(),
                getUserEmail(),
                getUserName(),
                getUserDepartment(),
                getUserRole()
        );
    }


    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Clear all data (logout)
    public void clear() {
        editor.clear();
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public String getAuthorizationHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }
}
