package com.example.pat_act5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private CardView cardViewRooms, cardMakeReservation, cardMyReservations, cardCheckIn, cardFeedback;
    private ImageView tvLogout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Set welcome message
        String userName = sharedPreferences.getString("name", "User");
        tvWelcome.setText("Welcome, " + userName + "!");

        // Set click listeners
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        cardViewRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, RoomsActivity.class));
            }
        });

        cardMakeReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, MakeReservationActivity.class));
            }
        });

        cardMyReservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, MyReservationsActivity.class));
            }
        });

        cardCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, CheckInActivity.class));
            }
        });

        cardFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, FeedbackActivity.class));
            }
        });
    }

private void initViews() {
        tvWelcome = findViewById(R.id.tvUserName);
        tvLogout = findViewById(R.id.tvLogout);
        cardViewRooms = findViewById(R.id.cardViewRooms);
        cardMakeReservation = findViewById(R.id.cardMakeReservation);
        cardMyReservations = findViewById(R.id.cardMyReservations);
        cardCheckIn= findViewById(R.id.cardCheckIn);
        cardFeedback = findViewById(R.id.cardFeedback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Go back to login activity
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}