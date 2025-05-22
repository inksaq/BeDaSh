package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ClientGoalSetupActivity extends BaseActivity {
    private static final String TAG = "ClientGoalSetupActivity";

    private Client client;
    private int recommendedCalories;
    private TextView tvRecommendedIntake;
    private EditText etTargetIntake;
    private Button setTargetIntakeButton;
    private Button completeProfileButton;

    private ImageButton btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_goal_setup);
        setupBase();

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
        btn_back = findViewById(R.id.btn_back);

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
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity to go to the previous one on the stack
                finish();
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

        // Use the FirestoreManager to save the client
        mFirestoreManager.saveClient(client, new FirestoreManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String clientId) {
                Log.d(TAG, "Client saved successfully with ID: " + clientId);
                Toast.makeText(ClientGoalSetupActivity.this,
                        "Client profile created successfully!",
                        Toast.LENGTH_LONG).show();
                returnToDashboard();
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error saving client", error);
                Toast.makeText(ClientGoalSetupActivity.this,
                        "Failed to save client profile: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void returnToDashboard() {
        // Return to the dashboard
        Intent intent = new Intent(ClientGoalSetupActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}