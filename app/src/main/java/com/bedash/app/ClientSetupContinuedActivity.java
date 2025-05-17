package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ClientSetupContinuedActivity extends BaseActivity {
    private Client client;
    private Spinner spinnerEthnicity;
    private Spinner spinnerDietaryPreferences;
    private EditText etAdditionalNotes;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_setup_continued);
        setupBase();

        // Get client data from previous screen
        if (getIntent().hasExtra("client")) {
            client = (Client) getIntent().getSerializableExtra("client");
        } else {
            // If no client data, go back to first screen
            Toast.makeText(this, "Error: No client data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinners();
        setupButtons();
    }

    private void initializeViews() {
        spinnerEthnicity = findViewById(R.id.spinner_ethnicity);
        spinnerDietaryPreferences = findViewById(R.id.spinner_dietary_preferences);
        etAdditionalNotes = findViewById(R.id.et_additional_notes);
        nextButton = findViewById(R.id.btn_next);
    }

    private void setupSpinners() {
        // Set up ethnicity spinner
        ArrayAdapter<CharSequence> ethnicityAdapter = ArrayAdapter.createFromResource(
                this, R.array.ethnicity_array, android.R.layout.simple_spinner_item);
        ethnicityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEthnicity.setAdapter(ethnicityAdapter);

        // Set up dietary preferences spinner
        ArrayAdapter<CharSequence> dietaryAdapter = ArrayAdapter.createFromResource(
                this, R.array.dietary_preferences_array, android.R.layout.simple_spinner_item);
        dietaryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDietaryPreferences.setAdapter(dietaryAdapter);
    }

    private void setupButtons() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update client object with new information
                client.setEthnicity(spinnerEthnicity.getSelectedItem().toString());
                client.setDietaryPreferences(spinnerDietaryPreferences.getSelectedItem().toString());

                // Calculate recommended calories based on client data
                int recommendedCalories = client.calculateRecommendedCalories();

                // Start the next activity with client data
                Intent intent = new Intent(ClientSetupContinuedActivity.this, ClientGoalSetupActivity.class);
                intent.putExtra("client", client);
                intent.putExtra("recommendedCalories", recommendedCalories);
                startActivity(intent);
            }
        });
    }
}