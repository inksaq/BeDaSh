package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class AddFoodActivity extends BaseActivity {
    private static final String TAG = "AddFoodActivity";

    private ImageButton backButton;
    private TextView selectedItemText;
    private TextView caloriesPerServingText;
    private TextView servingSizeText;
    private EditText servingAmountEditText;
    private TextView totalCaloriesText;
    private TextView nutritionPreviewText;
    private Button addToLogButton;
    private Spinner mealCategorySpinner;
    private Spinner timeSpinner;
    private String[] timeOptions;
    private String clientId;
    private String clientName;
    private String selectedDate;
    private Food selectedFood;
    private FirestoreManager mFirestoreManager;

    //meal category options
    private String[] mealCategories = {"Breakfast", "Lunch", "Dinner", "Snack"};
    //time options
        private void generateTimeOptions() {
        ArrayList<String> times = new ArrayList<>();

            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute += 15) {
                    String time24 = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

                    // Convert to 12-hour format for display
                    String displayTime;
                    if (hour == 0) {
                        displayTime = String.format(Locale.getDefault(), "12:%02d AM", minute);
                    } else if (hour < 12) {
                        displayTime = String.format(Locale.getDefault(), "%d:%02d AM", hour, minute);
                    } else if (hour == 12) {
                        displayTime = String.format(Locale.getDefault(), "12:%02d PM", minute);
                    } else {
                        displayTime = String.format(Locale.getDefault(), "%d:%02d PM", hour - 12, minute);
                    }

                    times.add(displayTime);
                }
            }

            timeOptions = times.toArray(new String[0]);
        }

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
        updateNutritionPreview();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        selectedItemText = findViewById(R.id.selected_item_text);
        caloriesPerServingText = findViewById(R.id.calories_per_serving_text);
        servingSizeText = findViewById(R.id.serving_size_text);
        servingAmountEditText = findViewById(R.id.serving_amount_edit_text);
        totalCaloriesText = findViewById(R.id.total_calories_text);
        nutritionPreviewText = findViewById(R.id.nutrition_preview_text);
        addToLogButton = findViewById(R.id.add_to_log_button);
        mealCategorySpinner = findViewById(R.id.meal_category_spinner);
        timeSpinner = findViewById(R.id.time_spinner);

        generateTimeOptions();

        // Setup time spinner
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeOptions);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        setDefaultTime();

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

    private void setDefaultTime() {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);

        // Round to nearest 15 minutes
        int roundedMinute = ((currentMinute + 7) / 15) * 15;
        if (roundedMinute >= 60) {
            roundedMinute = 0;
            currentHour++;
            if (currentHour >= 24) {
                currentHour = 0;
            }
        }
        // Find matching time in options array
        String targetTime;
        if (currentHour == 0) {
            targetTime = String.format(Locale.getDefault(), "12:%02d AM", roundedMinute);
        } else if (currentHour < 12) {
            targetTime = String.format(Locale.getDefault(), "%d:%02d AM", currentHour, roundedMinute);
        } else if (currentHour == 12) {
            targetTime = String.format(Locale.getDefault(), "12:%02d PM", roundedMinute);
        } else {
            targetTime = String.format(Locale.getDefault(), "%d:%02d PM", currentHour - 12, roundedMinute);
        }

        // Set the spinner to the current time
        for (int i = 0; i < timeOptions.length; i++) {
            if (timeOptions[i].equals(targetTime)) {
                timeSpinner.setSelection(i);
                break;
            }
        }
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

        // Update total calories and nutrition preview when serving amount changes
        servingAmountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalCalories();
                updateNutritionPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
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

    private void updateNutritionPreview() {
        try {
            String servingAmountStr = servingAmountEditText.getText().toString().trim();
            if (!servingAmountStr.isEmpty()) {
                double servingAmount = Double.parseDouble(servingAmountStr);

                StringBuilder nutritionText = new StringBuilder("Nutrition Preview:\n");

                // Macronutrients
                if (selectedFood.getProtein() > 0 || selectedFood.getTotalCarbohydrates() > 0 || selectedFood.getTotalFat() > 0) {
                    nutritionText.append("Macronutrients:\n");

                    if (selectedFood.getProtein() > 0) {
                        nutritionText.append(String.format(Locale.getDefault(),
                                "• Protein: %.1fg\n", servingAmount * selectedFood.getProtein()));
                    }

                    if (selectedFood.getTotalCarbohydrates() > 0) {
                        nutritionText.append(String.format(Locale.getDefault(),
                                "• Carbs: %.1fg", servingAmount * selectedFood.getTotalCarbohydrates()));

                        if (selectedFood.getDietaryFiber() > 0) {
                            nutritionText.append(String.format(Locale.getDefault(),
                                    " (Fiber: %.1fg)", servingAmount * selectedFood.getDietaryFiber()));
                        }
                        nutritionText.append("\n");
                    }

                    if (selectedFood.getTotalFat() > 0) {
                        nutritionText.append(String.format(Locale.getDefault(),
                                "• Fat: %.1fg", servingAmount * selectedFood.getTotalFat()));

                        if (selectedFood.getSaturatedFat() > 0) {
                            nutritionText.append(String.format(Locale.getDefault(),
                                    " (Sat: %.1fg)", servingAmount * selectedFood.getSaturatedFat()));
                        }
                        nutritionText.append("\n");
                    }
                }

                // Key micronutrients
                boolean hasMicronutrients = false;
                StringBuilder microText = new StringBuilder();

                if (selectedFood.getIron() > 0) {
                    microText.append(String.format(Locale.getDefault(),
                            "• Iron: %.1fmg\n", servingAmount * selectedFood.getIron()));
                    hasMicronutrients = true;
                }

                if (selectedFood.getVitaminD() > 0) {
                    microText.append(String.format(Locale.getDefault(),
                            "• Vitamin D: %.1fmcg\n", servingAmount * selectedFood.getVitaminD()));
                    hasMicronutrients = true;
                }

                if (selectedFood.getVitaminB12() > 0) {
                    microText.append(String.format(Locale.getDefault(),
                            "• Vitamin B12: %.1fmcg\n", servingAmount * selectedFood.getVitaminB12()));
                    hasMicronutrients = true;
                }

                if (selectedFood.getCalcium() > 0) {
                    microText.append(String.format(Locale.getDefault(),
                            "• Calcium: %.0fmg\n", servingAmount * selectedFood.getCalcium()));
                    hasMicronutrients = true;
                }

                if (selectedFood.getVitaminC() > 0) {
                    microText.append(String.format(Locale.getDefault(),
                            "• Vitamin C: %.1fmg\n", servingAmount * selectedFood.getVitaminC()));
                    hasMicronutrients = true;
                }

                if (selectedFood.getSodium() > 0) {
                    microText.append(String.format(Locale.getDefault(),
                            "• Sodium: %.0fmg\n", servingAmount * selectedFood.getSodium()));
                    hasMicronutrients = true;
                }

                if (hasMicronutrients) {
                    nutritionText.append("\nKey Nutrients:\n").append(microText);
                }

                // If no detailed nutrition available
                if (nutritionText.toString().equals("Nutrition Preview:\n")) {
                    nutritionText.append("No detailed nutrition information available.");
                }

                nutritionPreviewText.setText(nutritionText.toString());
                nutritionPreviewText.setVisibility(View.VISIBLE);
            } else {
                nutritionPreviewText.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            nutritionPreviewText.setVisibility(View.GONE);
        }
    }

    private void addFoodToLog() {
        double servingAmount = Double.parseDouble(servingAmountEditText.getText().toString().trim());


        String selectedMealCategory = mealCategories[mealCategorySpinner.getSelectedItemPosition()];
        String selectedTime = timeOptions[timeSpinner.getSelectedItemPosition()];

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

                runOnUiThread(() -> {
                    Toast.makeText(AddFoodActivity.this,
                            "Food added to log successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate back to FoodLogActivity
                    Intent intent = new Intent(AddFoodActivity.this, FoodLogActivity.class);
                    intent.putExtra("client_id", clientId);
                    intent.putExtra("client_name", clientName);
                    if (selectedDate != null) {
                        intent.putExtra("selected_date", selectedDate);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error adding food entry", error);

                runOnUiThread(() -> {
                    Toast.makeText(AddFoodActivity.this,
                            "Error adding food to log: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Re-enable button
                    addToLogButton.setEnabled(true);
                    addToLogButton.setText("Add to food log");
                });
            }
        });
    }
}