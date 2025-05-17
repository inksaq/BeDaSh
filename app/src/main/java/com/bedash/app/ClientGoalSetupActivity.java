package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ClientGoalSetupActivity extends BaseActivity {
    private static final String TAG = "ClientGoalSetupActivity";

    private Client client;
    private int recommendedCalories;
    private TextView tvRecommendedIntake;
    private EditText etTargetIntake;
    private Button setTargetIntakeButton;
    private Button completeProfileButton;

    // Firebase
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_goal_setup);
        setupBase();

        // Initialize Firebase Database with diagnostics
        initializeDatabase();

        // Get client data and recommended calories from previous screen
        if (getIntent().hasExtra("client") && getIntent().hasExtra("recommendedCalories")) {
            client = (Client) getIntent().getSerializableExtra("client");
            recommendedCalories = getIntent().getIntExtra("recommendedCalories", 2000);
        } else {
            // If no client data, go back to first screen
            Toast.makeText(this, "Error: No client data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        updateUIWithRecommendedIntake();
        setupButtons();
    }

    private void initializeDatabase() {
        try {
            // Get database instance and reference
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            mDatabase = database.getReference();

            // Check connection
            DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class) != null &&
                            Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                    if (connected) {
                        Log.d(TAG, "Connected to Firebase Database");
                    } else {
                        Log.w(TAG, "Disconnected from Firebase Database");
                        Toast.makeText(ClientGoalSetupActivity.this,
                                "Warning: Database connection is offline. Your changes may not be saved immediately.",
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase connection listener was cancelled", error.toException());
                }
            });

            // Test database access
            mDatabase.child(".info/serverTimeOffset").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "Successfully accessed Firebase: got server time offset");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to access Firebase", error.toException());
                    Toast.makeText(ClientGoalSetupActivity.this,
                            "Error: Can't connect to database, check your connection: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Database", e);
            Toast.makeText(this,
                    "Error initializing database: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        tvRecommendedIntake = findViewById(R.id.tv_recommended_intake);
        etTargetIntake = findViewById(R.id.et_target_intake);
        setTargetIntakeButton = findViewById(R.id.btn_set_target_intake);
        completeProfileButton = findViewById(R.id.btn_complete_profile);
    }

    private void updateUIWithRecommendedIntake() {
        String recommendedText = "The recommended intake for this client is:\n\n" +
                "(Calories) to Maintain: " + recommendedCalories + " kcal per day";
        tvRecommendedIntake.setText(recommendedText);

        // Pre-fill the target intake field with the recommended calories
        etTargetIntake.setText(String.valueOf(recommendedCalories));
    }

    private void setupButtons() {
        setTargetIntakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    int targetIntake = Integer.parseInt(etTargetIntake.getText().toString().trim());
                    client.setGoalCalories(targetIntake);

                    // Set default macronutrient breakdown (40% carbs, 30% protein, 30% fat)
                    int proteinGrams = Math.round((targetIntake * 0.3f) / 4); // 4 calories per gram of protein
                    int carbsGrams = Math.round((targetIntake * 0.4f) / 4);  // 4 calories per gram of carbs
                    int fatGrams = Math.round((targetIntake * 0.3f) / 9);    // 9 calories per gram of fat

                    client.setGoalProtein(proteinGrams);
                    client.setGoalCarbs(carbsGrams);
                    client.setGoalFat(fatGrams);

                    Toast.makeText(ClientGoalSetupActivity.this,
                            "Target intake set to " + targetIntake + " calories",
                            Toast.LENGTH_SHORT).show();

                    completeProfileButton.setEnabled(true);
                }
            }
        });

        completeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveClientToDatabase();
            }
        });
    }

    private boolean validateInput() {
        String targetIntakeStr = etTargetIntake.getText().toString().trim();
        if (targetIntakeStr.isEmpty()) {
            etTargetIntake.setError("Please enter target calorie intake");
            return false;
        }

        try {
            int targetIntake = Integer.parseInt(targetIntakeStr);
            if (targetIntake < 1000 || targetIntake > 5000) {
                etTargetIntake.setError("Please enter a reasonable calorie intake (1000-5000)");
                return false;
            }
        } catch (NumberFormatException e) {
            etTargetIntake.setError("Please enter a valid number");
            return false;
        }

        return true;
    }

    private void saveClientToDatabase() {
        // Show progress
        Toast.makeText(this, "Saving client profile...", Toast.LENGTH_SHORT).show();

        try {
            // Check if database reference is valid
            if (mDatabase == null) {
                Log.e(TAG, "Database reference is null, trying to reinitialize");
                mDatabase = FirebaseDatabase.getInstance().getReference();
            }

            // Generate a unique ID for the client
            if (client.getId() == null || client.getId().isEmpty()) {
                String key = mDatabase.child("clients").push().getKey();
                if (key == null) {
                    Toast.makeText(this, "Error: Failed to generate client ID", Toast.LENGTH_LONG).show();
                    return;
                }
                client.setId(key);
                Log.d(TAG, "Generated client ID: " + key);
            }

            // Get current user ID as nurse ID
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Error: You must be logged in to save client data",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String nurseId = currentUser.getUid();
            Log.d(TAG, "Using nurse ID: " + nurseId);

            // Create a Map to store in Firebase
            Map<String, Object> clientValues = new HashMap<>();
            clientValues.put("id", client.getId());
            clientValues.put("name", client.getName());
            clientValues.put("age", client.getAge());
            clientValues.put("weight", client.getWeight());
            clientValues.put("height", client.getHeight());
            clientValues.put("gender", client.getGender());
            clientValues.put("activityLevel", client.getActivityLevel());

            if (client.getEthnicity() != null) {
                clientValues.put("ethnicity", client.getEthnicity());
            }

            if (client.getDietaryPreferences() != null) {
                clientValues.put("dietaryPreferences", client.getDietaryPreferences());
            }

            clientValues.put("goalCalories", client.getGoalCalories());
            clientValues.put("goalProtein", client.getGoalProtein());
            clientValues.put("goalCarbs", client.getGoalCarbs());
            clientValues.put("goalFat", client.getGoalFat());
            clientValues.put("nurseId", nurseId);
            clientValues.put("createdAt", ServerValue.TIMESTAMP);

            Log.d(TAG, "Saving client: " + clientValues.toString());

            // Test write a small value first to verify database connectivity
            mDatabase.child("test").setValue("test_" + System.currentTimeMillis())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Test write successful, proceeding with client save");

                        // Save client to database
                        mDatabase.child("clients").child(client.getId()).setValue(clientValues)
                                .addOnSuccessListener(aVoid2 -> {
                                    Log.d(TAG, "Client data saved successfully");

                                    // Also save reference to this client in the nurse's clients list
                                    mDatabase.child("nurses").child(nurseId).child("clients")
                                            .child(client.getId()).setValue(true)
                                            .addOnSuccessListener(aVoid3 -> {
                                                Log.d(TAG, "Nurse reference saved successfully");

                                                // Both writes were successful
                                                Toast.makeText(ClientGoalSetupActivity.this,
                                                        "Client profile created successfully!",
                                                        Toast.LENGTH_LONG).show();
                                                returnToDashboard();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Error writing nurse reference", e);

                                                // Even if nurse reference fails, client was saved
                                                Toast.makeText(ClientGoalSetupActivity.this,
                                                        "Client saved but nurse reference failed: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                                returnToDashboard();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error writing client data", e);
                                    Toast.makeText(ClientGoalSetupActivity.this,
                                            "Failed to save client profile: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Test write failed", e);
                        Toast.makeText(ClientGoalSetupActivity.this,
                                "Database connection test failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception during client save", e);
            Toast.makeText(this,
                    "Error saving client: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void returnToDashboard() {
        // Return to the dashboard
        Intent intent = new Intent(ClientGoalSetupActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}