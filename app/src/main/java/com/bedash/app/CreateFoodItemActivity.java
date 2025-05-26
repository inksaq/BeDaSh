package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class CreateFoodItemActivity extends BaseActivity {
    private static final String TAG = "CreateFoodItemActivity";

    private ImageButton backButton;
    private EditText nameEditText;
    private Spinner categorySpinner;
    private EditText servingSizeEditText;
    private EditText caloriesEditText;
    private Button createFoodButton;

    private String clientId;
    private String clientName;
    private String selectedDate;
    private FirestoreManager mFirestoreManager;

    // Food categories
    private String[] categories = {
            "Fruits", "Vegetables", "Grains", "Protein", "Dairy",
            "Fats & Oils", "Beverages", "Snacks", "Desserts", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_food_item);
        setupBase();

        // Initialize Firestore Manager
        mFirestoreManager = FirestoreManager.getInstance();

        // Get passed data
        if (getIntent().hasExtra("client_id") && getIntent().hasExtra("client_name")) {
            clientId = getIntent().getStringExtra("client_id");
            clientName = getIntent().getStringExtra("client_name");
            selectedDate = getIntent().getStringExtra("selected_date");
        } else {
            Toast.makeText(this, "Error: Missing required data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        nameEditText = findViewById(R.id.name_edit_text);
        categorySpinner = findViewById(R.id.category_spinner);
        servingSizeEditText = findViewById(R.id.serving_size_edit_text);
        caloriesEditText = findViewById(R.id.calories_edit_text);
        createFoodButton = findViewById(R.id.create_food_button);

        // Setup category spinner
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        createFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    createFoodItem();
                }
            }
        });
    }

    private boolean validateInput() {
        String name = nameEditText.getText().toString().trim();
        String servingSize = servingSizeEditText.getText().toString().trim();
        String caloriesStr = caloriesEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Food name is required");
            nameEditText.requestFocus();
            return false;
        }

        if (servingSize.isEmpty()) {
            servingSizeEditText.setError("Serving size is required");
            servingSizeEditText.requestFocus();
            return false;
        }

        if (caloriesStr.isEmpty()) {
            caloriesEditText.setError("Calories per serving is required");
            caloriesEditText.requestFocus();
            return false;
        }

        try {
            double calories = Double.parseDouble(caloriesStr);
            if (calories < 0) {
                caloriesEditText.setError("Calories must be a positive number");
                caloriesEditText.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            caloriesEditText.setError("Please enter a valid number for calories");
            caloriesEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void createFoodItem() {
        String name = nameEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String servingSize = servingSizeEditText.getText().toString().trim();
        double calories = Double.parseDouble(caloriesEditText.getText().toString().trim());

        // Create Food object
        Food food = new Food(name, category, calories, servingSize);

        // Show progress
        createFoodButton.setEnabled(false);
        createFoodButton.setText("Creating...");

        // Save to Firestore
        mFirestoreManager.saveFood(food, new FirestoreManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String foodId) {
                Log.d(TAG, "Food item created successfully: " + name);
                Toast.makeText(CreateFoodItemActivity.this,
                        "Food item created successfully!", Toast.LENGTH_SHORT).show();

                // Set the ID in the food object
                food.setId(foodId);

                // Navigate to AddFoodActivity with the newly created food
                Intent intent = new Intent(CreateFoodItemActivity.this, AddFoodActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                intent.putExtra("selected_date", selectedDate);
                intent.putExtra("selected_food", food);
                startActivity(intent);
                finish(); // Close this activity
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error creating food item", error);
                Toast.makeText(CreateFoodItemActivity.this,
                        "Error creating food item: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();

                // Re-enable button
                createFoodButton.setEnabled(true);
                createFoodButton.setText("Create Food");
            }
        });
    }
}