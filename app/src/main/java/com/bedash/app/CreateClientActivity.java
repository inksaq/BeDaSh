package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class CreateClientActivity extends BaseActivity {
    private EditText etClientName;
    private EditText etClientAge;
    private EditText etClientWeight;
    private EditText etClientHeight;
    private Spinner spinnerGender;
    private Spinner spinnerActivity;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_client);
        setupBase();

        initializeViews();
        setupSpinners();
        setupButtons();
    }

    private void initializeViews() {
        etClientName = findViewById(R.id.et_client_name);
        etClientAge = findViewById(R.id.et_client_age);
        etClientWeight = findViewById(R.id.et_client_weight);
        etClientHeight = findViewById(R.id.et_client_height);
        spinnerGender = findViewById(R.id.spinner_gender);
        spinnerActivity = findViewById(R.id.spinner_activity);
        nextButton = findViewById(R.id.btn_next);
    }

    private void setupSpinners() {
        // Set up gender spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Set up activity level spinner
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(
                this, R.array.activity_levels_array, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActivity.setAdapter(activityAdapter);
    }

    private void setupButtons() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    // Create Client object to pass to next step
                    Client client = new Client();
                    client.setName(etClientName.getText().toString());
                    client.setAge(Integer.parseInt(etClientAge.getText().toString()));
                    client.setWeight(Float.parseFloat(etClientWeight.getText().toString()));
                    client.setHeight(Float.parseFloat(etClientHeight.getText().toString()));
                    client.setGender(spinnerGender.getSelectedItem().toString());
                    client.setActivityLevel(spinnerActivity.getSelectedItem().toString());

                    // Start the next activity with client data
                    Intent intent = new Intent(CreateClientActivity.this, ClientSetupContinuedActivity.class);
                    intent.putExtra("client", client);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (etClientName.getText().toString().trim().isEmpty()) {
            etClientName.setError("Name is required");
            isValid = false;
        }

        if (etClientAge.getText().toString().trim().isEmpty()) {
            etClientAge.setError("Age is required");
            isValid = false;
        }

        if (etClientWeight.getText().toString().trim().isEmpty()) {
            etClientWeight.setError("Weight is required");
            isValid = false;
        }

        if (etClientHeight.getText().toString().trim().isEmpty()) {
            etClientHeight.setError("Height is required");
            isValid = false;
        }

        return isValid;
    }
}