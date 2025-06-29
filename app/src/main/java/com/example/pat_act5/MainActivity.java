package com.example.pat_act5;

import android.os.Bundle;

import android.util.Base64;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Check if user is already logged in
        checkLoginStatus();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.Email);
        etPassword = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.Register);
    }

    private void checkLoginStatus() {
        String token = sharedPreferences.getString("token", "");
        if (!token.isEmpty()) {
            // User is already logged in, go to dashboard
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
        }
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("email", email);
            loginData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = ApiConfig.LOGIN_URL;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, loginData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token = response.getString("token");
                            String role = response.getString("role");
                            String userId = extractUserIdFromToken(token);

                            // Save token and user info
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("token", token);
                            editor.putString("email", email);
                            editor.putString("role", role);
                            editor.putString("userId", userId);
                            editor.apply();

                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Go to dashboard
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(request);
    }

    /**
     * Extract userId from JWT token payload
     * @param token JWT token
     * @return userId string
     */
    private String extractUserIdFromToken(String token) {
        try {
            // Split token into parts (header.payload.signature)
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length >= 2) {
                // Decode payload (second part)
                String payload = tokenParts[1];

                // Add padding if needed for Base64 decoding
                while (payload.length() % 4 != 0) {
                    payload += "=";
                }

                // Decode Base64
                byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
                String decodedPayload = new String(decodedBytes);

                // Parse JSON to get userId
                JSONObject payloadJson = new JSONObject(decodedPayload);
                return payloadJson.getString("userId");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ""; // Return empty string if extraction fails
    }

    /**
     * Utility method to get saved userId from SharedPreferences
     * This can be called from other activities
     */
    public static String getSavedUserId(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("userId", "");
    }
}