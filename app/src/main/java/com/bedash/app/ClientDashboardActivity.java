package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;

public class ClientDashboardActivity extends BaseActivity {
    private static final String TAG = "ClientDashboardActivity";

    private TextView clientNameTextView;
    private TextView clientDetailsTextView;
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
        clientDetailsTextView = findViewById(R.id.client_details);
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
        // Show loading state
        Toast.makeText(this, "Loading data for client: " + clientName, Toast.LENGTH_SHORT).show();

        // Use FirestoreManager to load client data
        mFirestoreManager.getClient(clientId, new FirestoreManager.DatabaseCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Update UI with client details
                    updateClientDetails(documentSnapshot);
                } else {
                    Toast.makeText(ClientDashboardActivity.this,
                            "Client data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading client data", error);
                Toast.makeText(ClientDashboardActivity.this,
                        "Error loading client data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateClientDetails(DocumentSnapshot documentSnapshot) {
        StringBuilder details = new StringBuilder();

        // Age
        if (documentSnapshot.contains("age")) {
            details.append("Age: ").append(documentSnapshot.getLong("age")).append("\n");
        }

        // Height and weight
        if (documentSnapshot.contains("height") && documentSnapshot.contains("weight")) {
            details.append("Height: ").append(documentSnapshot.getDouble("height")).append(" cm\n");
            details.append("Weight: ").append(documentSnapshot.getDouble("weight")).append(" kg\n");
        }

        // Goal calories
        if (documentSnapshot.contains("goalCalories")) {
            details.append("\nCalorie Goal: ").append(documentSnapshot.getLong("goalCalories")).append(" kcal\n");
        }

        // Macros
        if (documentSnapshot.contains("goalProtein") &&
                documentSnapshot.contains("goalCarbs") &&
                documentSnapshot.contains("goalFat")) {

            details.append("Protein: ").append(documentSnapshot.getLong("goalProtein")).append("g\n");
            details.append("Carbs: ").append(documentSnapshot.getLong("goalCarbs")).append("g\n");
            details.append("Fat: ").append(documentSnapshot.getLong("goalFat")).append("g\n");
        }

        // Set the details to the TextView if it exists
        if (clientDetailsTextView != null) {
            clientDetailsTextView.setText(details.toString());
        }
    }

    @Override
    protected void handleHomeClick() {
        // Override from BaseActivity to handle home button click
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}