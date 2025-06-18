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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Locale;

public class AddFoodActivity extends BaseActivity {
    private static final String TAG = "AddFoodActivity";

    private ImageButton backButton;
    private TextView selectedItemText;
    private TextView caloriesPerServingText;
    private TextView servingSizeText;
    private EditText servingAmountEditText;
    private TextView totalCaloriesText;
    private Button addToLogButton;
    private Spinner mealCategorySpinner;

    private String clientId;
    private String clientName;
    private String selectedDate;
    private Food selectedFood;
    private FirestoreManager mFirestoreManager;

    //meal category options
    private String[] mealCategories = {"Breakfast", "Lunch", "Dinner", "Snack"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);
        setupBase();

        // Initialize Firestore Manager
        mFirestoreManager = FirestoreManager.getInstance();

        // Get passed data
        if (getIntent().hasExtra("client_id") && getIntent().hasExtra("client_name") &&
                getIntent().hasExtra("selected_food")) {
            clientId = getIntent().getStringExtra("client_id");
            clientName = getIntent().getStringExtra("client_name");
            selectedDate = getIntent().getStringExtra("selected_date");
            selectedFood = (Food) getIntent().getSerializableExtra("selected_food");
        } else {
            Toast.makeText(this, "Error: Missing required data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        updateTotalCalories();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        selectedItemText = findViewById(R.id.selected_item_text);
        caloriesPerServingText = findViewById(R.id.calories_per_serving_text);
        servingSizeText = findViewById(R.id.serving_size_text);
        servingAmountEditText = findViewById(R.id.serving_amount_edit_text);
        totalCaloriesText = findViewById(R.id.total_calories_text);
        addToLogButton = findViewById(R.id.add_to_log_button);
        mealCategorySpinner = findViewById(R.id.meal_category_spinner);

        //meal category spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mealCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mealCategorySpinner.setAdapter(adapter);

        //set default meal
        mealCategorySpinner.setSelection(0);

        // Set food information
        selectedItemText.setText("Selected Item: " + selectedFood.getName());
        caloriesPerServingText.setText(String.format(Locale.getDefault(),
                "%.0f calories per serving", selectedFood.getCaloriesPerServing()));
        servingSizeText.setText("(1 serving = " + selectedFood.getServingSize() + ")");

        // Set default serving amount to 1
        servingAmountEditText.setText("1.0");
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addToLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    addFoodToLog();
                }
            }
        });

        // Update total calories when serving amount changes
        servingAmountEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    updateTotalCalories();
                }
            }
        });
    }

    private boolean validateInput() {
        String servingAmountStr = servingAmountEditText.getText().toString().trim();

        if (servingAmountStr.isEmpty()) {
            servingAmountEditText.setError("Serving amount is required");
            servingAmountEditText.requestFocus();
            return false;
        }

        try {
            double servingAmount = Double.parseDouble(servingAmountStr);
            if (servingAmount <= 0) {
                servingAmountEditText.setError("Serving amount must be greater than 0");
                servingAmountEditText.requestFocus();
                return false;
            }
            if (servingAmount > 100) {
                servingAmountEditText.setError("Serving amount seems too large");
                servingAmountEditText.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            servingAmountEditText.setError("Please enter a valid number");
            servingAmountEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void updateTotalCalories() {
        try {
            String servingAmountStr = servingAmountEditText.getText().toString().trim();
            if (!servingAmountStr.isEmpty()) {
                double servingAmount = Double.parseDouble(servingAmountStr);
                double totalCalories = servingAmount * selectedFood.getCaloriesPerServing();
                totalCaloriesText.setText(String.format(Locale.getDefault(),
                        "Total calories: %.0f", totalCalories));
            }
        } catch (NumberFormatException e) {
            totalCaloriesText.setText("Total calories: --");
        }
    }

    private void addFoodToLog() {
        double servingAmount = Double.parseDouble(servingAmountEditText.getText().toString().trim());

        String selectedMealCategory = mealCategories[mealCategorySpinner.getSelectedItemPosition()];

        // Create FoodEntry
        FoodEntry foodEntry = new FoodEntry(
                clientId,
                selectedFood.getId(),
                selectedFood.getName(),
                servingAmount,
                selectedFood.getCaloriesPerServing(),
                selectedMealCategory
        );

        // Override the date with the selected date if provided
        if (selectedDate != null && !selectedDate.isEmpty()) {
            foodEntry.setDate(selectedDate);
        }

        // Show progress
        addToLogButton.setEnabled(false);
        addToLogButton.setText("Adding...");

        // Save to Firestore
        mFirestoreManager.saveFoodEntry(foodEntry, new FirestoreManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String entryId) {
                Log.d(TAG, "Food entry added successfully");
                Toast.makeText(AddFoodActivity.this,
                        "Food added to log successfully!", Toast.LENGTH_SHORT).show();

                // Navigate back to FoodLogActivity
                Intent intent = new Intent(AddFoodActivity.this, FoodLogActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error adding food entry", error);
                Toast.makeText(AddFoodActivity.this,
                        "Error adding food to log: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();

                // Re-enable button
                addToLogButton.setEnabled(true);
                addToLogButton.setText("Add to food log");
            }
        });
    }
}