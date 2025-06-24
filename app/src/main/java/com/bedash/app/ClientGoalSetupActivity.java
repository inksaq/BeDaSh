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

public class ClientGoalSetupActivity extends BaseActivity {
    private static final String TAG = "ClientGoalSetupActivity";

    private Client client;
    private int recommendedCalories;
    private TextView tvRecommendedIntake;
    private EditText etTargetIntake;
    private Button completeProfileButton;

    // Firestore
    private FirestoreManager mFirestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_goal_setup);
        setupBase();

        // Initialize Firestore Manager
        mFirestoreManager = FirestoreManager.getInstance();

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

    private void initializeViews() {
        tvRecommendedIntake = findViewById(R.id.tv_recommended_intake);
        etTargetIntake = findViewById(R.id.et_target_intake);
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
        completeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInput()) {
                    setClientGoals();
                    saveClientToDatabase();
                }
            }
        });
    }


    private void setClientGoals() {
                    int targetIntake = Integer.parseInt(etTargetIntake.getText().toString().trim());
                    client.setGoalCalories(targetIntake);

                    // Set default macronutrient breakdown (40% carbs, 30% protein, 30% fat)
                    int proteinGrams = Math.round((targetIntake * 0.3f) / 4); // 4 calories per gram of protein
                    int carbsGrams = Math.round((targetIntake * 0.4f) / 4);  // 4 calories per gram of carbs
                    int fatGrams = Math.round((targetIntake * 0.3f) / 9);    // 9 calories per gram of fat

                    client.setGoalProtein(proteinGrams);
                    client.setGoalCarbs(carbsGrams);
                    client.setGoalFat(fatGrams);

        Log.d(TAG, "Client goals set - Calories: " + targetIntake +
                ", Protein: " + proteinGrams + "g, Carbs: " + carbsGrams + "g, Fat: " + fatGrams + "g");
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
            // Save client using FirestoreManager
            mFirestoreManager.saveClient(client, new FirestoreManager.DatabaseCallback<String>() {
                @Override
                public void onSuccess(String clientId) {
                    Log.d(TAG, "Client data saved successfully with ID: " + clientId);
                    Toast.makeText(ClientGoalSetupActivity.this,
                            "Client profile created successfully!",
                            Toast.LENGTH_LONG).show();
                    returnToDashboard();
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error saving client data", error);
                    Toast.makeText(ClientGoalSetupActivity.this,
                            "Failed to save client profile: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
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