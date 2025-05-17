package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

public class ClientDashboardActivity extends BaseActivity {
    private TextView clientNameTextView;
    private TextView dayTextView;
    private Button foodLogButton;
    private ImageButton backButton;

    private String clientId;
    private String clientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);
        setupBase(); // Call the setup method from BaseActivity

        // Get client data passed from previous activity
        if (getIntent().hasExtra("client_id") && getIntent().hasExtra("client_name")) {
            clientId = getIntent().getStringExtra("client_id");
            clientName = getIntent().getStringExtra("client_name");
        } else {
            Toast.makeText(this, "Error: No client data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        loadClientData();
    }

    private void initializeViews() {
        clientNameTextView = findViewById(R.id.client_name_text);
        dayTextView = findViewById(R.id.day_text);
        foodLogButton = findViewById(R.id.food_log_button);
        backButton = findViewById(R.id.back_button);

        // Set client name
        clientNameTextView.setText("Client: " + clientName);

        // Set current day
        dayTextView.setText("Day: Today");
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        foodLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ClientDashboardActivity.this, "Food log feature coming soon", Toast.LENGTH_SHORT).show();
                // Future implementation: Navigate to food log activity
                // Intent intent = new Intent(ClientDashboardActivity.this, FoodLogActivity.class);
                // intent.putExtra("client_id", clientId);
                // startActivity(intent);
            }
        });
    }

    private void loadClientData() {
        // TODO: Load client data from Firebase
        // This would fetch the client's data, nutrition goals, etc.
        // For now, we'll just show a toast indicating this would happen
        Toast.makeText(this, "Loading data for client: " + clientName, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void handleHomeClick() {
        // Override from BaseActivity to handle home button click
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}