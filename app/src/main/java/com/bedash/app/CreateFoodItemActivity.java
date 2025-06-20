package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CreateFoodItemActivity extends BaseActivity {
    private static final String TAG = "CreateFoodItemActivity";

    // Basic UI elements
    private ImageButton backButton;
    private EditText nameEditText;
    private Spinner categorySpinner;
    private EditText servingSizeEditText;
    private EditText caloriesEditText;
    private Button createFoodButton;

    // Toggle sections
    private TextView macroToggleText;
    private TextView microToggleText;
    private LinearLayout macronutrientSection;
    private LinearLayout micronutrientSection;
    private boolean macroSectionExpanded = false;
    private boolean microSectionExpanded = false;

    // Macronutrient fields
    private EditText proteinEditText;
    private EditText totalCarbsEditText;
    private EditText fiberEditText;
    private EditText sugarsEditText;
    private EditText totalFatEditText;
    private EditText saturatedFatEditText;
    private EditText transFatEditText;

    // Critical micronutrient fields
    private EditText ironEditText;
    private EditText vitaminDEditText;
    private EditText vitaminB12EditText;
    private EditText folateEditText;
    private EditText magnesiumEditText;

    // Important micronutrient fields
    private EditText calciumEditText;
    private EditText zincEditText;
    private EditText potassiumEditText;
    private EditText sodiumEditText;
    private EditText vitaminCEditText;

    // Data
    private String clientId;
    private String clientName;
    private String selectedDate;
    private FirestoreManager mFirestoreManager;

    private String[] categories = {
            "Fruits", "Vegetables", "Grains", "Protein", "Dairy",
            "Fats & Oils", "Beverages", "Snacks", "Desserts", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_food_item);
        setupBase();

        mFirestoreManager = FirestoreManager.getInstance();

        // Get intent data
        getIntentData();

        // Initialize UI
        initializeViews();
        setupSpinner();
        setupToggleListeners();
        setupButtonListeners();
    }

    private void getIntentData() {
        clientId = getIntent().getStringExtra("client_id");
        clientName = getIntent().getStringExtra("client_name");
        selectedDate = getIntent().getStringExtra("selected_date");

        if (clientId == null || clientName == null) {
            Toast.makeText(this, "Error: Missing client information", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Basic fields
            backButton = findViewById(R.id.back_button);
            nameEditText = findViewById(R.id.name_edit_text);
            categorySpinner = findViewById(R.id.category_spinner);
            servingSizeEditText = findViewById(R.id.serving_size_edit_text);
            caloriesEditText = findViewById(R.id.calories_edit_text);
            createFoodButton = findViewById(R.id.create_food_button);

            // Toggle sections
            macroToggleText = findViewById(R.id.macro_toggle_text);
            microToggleText = findViewById(R.id.micro_toggle_text);
            macronutrientSection = findViewById(R.id.macronutrient_section);
            micronutrientSection = findViewById(R.id.micronutrient_section);

            // Macronutrient fields
            proteinEditText = findViewById(R.id.protein_edit_text);
            totalCarbsEditText = findViewById(R.id.total_carbs_edit_text);
            fiberEditText = findViewById(R.id.fiber_edit_text);
            sugarsEditText = findViewById(R.id.sugars_edit_text);
            totalFatEditText = findViewById(R.id.total_fat_edit_text);
            saturatedFatEditText = findViewById(R.id.saturated_fat_edit_text);
            transFatEditText = findViewById(R.id.trans_fat_edit_text);

            // Critical micronutrients
            ironEditText = findViewById(R.id.iron_edit_text);
            vitaminDEditText = findViewById(R.id.vitamin_d_edit_text);
            vitaminB12EditText = findViewById(R.id.vitamin_b12_edit_text);
            folateEditText = findViewById(R.id.folate_edit_text);
            magnesiumEditText = findViewById(R.id.magnesium_edit_text);

            // Important micronutrients
            calciumEditText = findViewById(R.id.calcium_edit_text);
            zincEditText = findViewById(R.id.zinc_edit_text);
            potassiumEditText = findViewById(R.id.potassium_edit_text);
            sodiumEditText = findViewById(R.id.sodium_edit_text);
            vitaminCEditText = findViewById(R.id.vitamin_c_edit_text);

            // Initially hide detailed sections
            if (macronutrientSection != null) {
                macronutrientSection.setVisibility(View.GONE);
            }
            if (micronutrientSection != null) {
                micronutrientSection.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error loading form. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupSpinner() {
        if (categorySpinner != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(adapter);
        }
    }

    private void setupToggleListeners() {
        if (macroToggleText != null) {
            macroToggleText.setOnClickListener(v -> toggleMacroSection());
        }

        if (microToggleText != null) {
            microToggleText.setOnClickListener(v -> toggleMicroSection());
        }
    }

    private void setupButtonListeners() {
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }

        if (createFoodButton != null) {
            createFoodButton.setOnClickListener(v -> {
                if (validateInput()) {
                    createFoodItem();
                }
            });
        }
    }

    private void toggleMacroSection() {
        if (macronutrientSection == null || macroToggleText == null) return;

        macroSectionExpanded = !macroSectionExpanded;
        macronutrientSection.setVisibility(macroSectionExpanded ? View.VISIBLE : View.GONE);
        macroToggleText.setText(macroSectionExpanded ?
                "▼ Macronutrients (tap to hide)" :
                "▶ Macronutrients (tap to add detailed nutrition)");
    }

    private void toggleMicroSection() {
        if (micronutrientSection == null || microToggleText == null) return;

        microSectionExpanded = !microSectionExpanded;
        micronutrientSection.setVisibility(microSectionExpanded ? View.VISIBLE : View.GONE);
        microToggleText.setText(microSectionExpanded ?
                "▼ Micronutrients (tap to hide)" :
                "▶ Micronutrients (tap to add vitamins & minerals)");
    }

    private boolean validateInput() {
        // Validate required fields
        if (nameEditText == null || nameEditText.getText().toString().trim().isEmpty()) {
            showError(nameEditText, "Food name is required");
            return false;
        }

        if (servingSizeEditText == null || servingSizeEditText.getText().toString().trim().isEmpty()) {
            showError(servingSizeEditText, "Serving size is required");
            return false;
        }

        if (caloriesEditText == null || caloriesEditText.getText().toString().trim().isEmpty()) {
            showError(caloriesEditText, "Calories per serving is required");
            return false;
        }

        // Validate calories
        try {
            double calories = Double.parseDouble(caloriesEditText.getText().toString().trim());
            if (calories < 0) {
                showError(caloriesEditText, "Calories must be a positive number");
                return false;
            }
        } catch (NumberFormatException e) {
            showError(caloriesEditText, "Please enter a valid number for calories");
            return false;
        }

        // Validate nutrition fields if sections are expanded
        if (macroSectionExpanded && !validateNutrientFields()) {
            return false;
        }

        if (microSectionExpanded && !validateMicronutrientFields()) {
            return false;
        }

        return true;
    }

    private boolean validateNutrientFields() {
        EditText[] macroFields = {proteinEditText, totalCarbsEditText, totalFatEditText,
                fiberEditText, sugarsEditText, saturatedFatEditText, transFatEditText};
        String[] macroNames = {"Protein", "Total carbohydrates", "Total fat",
                "Fiber", "Sugars", "Saturated fat", "Trans fat"};

        for (int i = 0; i < macroFields.length; i++) {
            if (macroFields[i] != null && !validateNutrientField(macroFields[i], macroNames[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean validateMicronutrientFields() {
        EditText[] microFields = {ironEditText, vitaminDEditText, vitaminB12EditText, folateEditText,
                magnesiumEditText, calciumEditText, zincEditText, potassiumEditText,
                sodiumEditText, vitaminCEditText};
        String[] microNames = {"Iron", "Vitamin D", "Vitamin B12", "Folate", "Magnesium",
                "Calcium", "Zinc", "Potassium", "Sodium", "Vitamin C"};

        for (int i = 0; i < microFields.length; i++) {
            if (microFields[i] != null && !validateNutrientField(microFields[i], microNames[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean validateNutrientField(EditText field, String fieldName) {
        String value = field.getText().toString().trim();
        if (!value.isEmpty()) {
            try {
                double numValue = Double.parseDouble(value);
                if (numValue < 0) {
                    showError(field, fieldName + " cannot be negative");
                    return false;
                }
            } catch (NumberFormatException e) {
                showError(field, "Please enter a valid number for " + fieldName);
                return false;
            }
        }
        return true;
    }

    private void showError(EditText field, String message) {
        if (field != null) {
            field.setError(message);
            field.requestFocus();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private double parseNutrientValue(EditText field) {
        if (field == null) return 0.0;

        String value = field.getText().toString().trim();
        if (value.isEmpty()) return 0.0;

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Error parsing nutrient value: " + value);
            return 0.0;
        }
    }

    private void createFoodItem() {
        try {
            // Get basic information
            String name = nameEditText.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            String servingSize = servingSizeEditText.getText().toString().trim();
            double calories = Double.parseDouble(caloriesEditText.getText().toString().trim());

            // Create Food object
            Food food = new Food(name, category, calories, servingSize);

            // Add macronutrients if section is expanded
            if (macroSectionExpanded) {
                food.setProtein(parseNutrientValue(proteinEditText));
                food.setTotalCarbohydrates(parseNutrientValue(totalCarbsEditText));
                food.setDietaryFiber(parseNutrientValue(fiberEditText));
                food.setTotalSugars(parseNutrientValue(sugarsEditText));
                food.setTotalFat(parseNutrientValue(totalFatEditText));
                food.setSaturatedFat(parseNutrientValue(saturatedFatEditText));
                food.setTransFat(parseNutrientValue(transFatEditText));
            }

            // Add micronutrients if section is expanded
            if (microSectionExpanded) {
                // Critical micronutrients
                food.setIron(parseNutrientValue(ironEditText));
                food.setVitaminD(parseNutrientValue(vitaminDEditText));
                food.setVitaminB12(parseNutrientValue(vitaminB12EditText));
                food.setFolate(parseNutrientValue(folateEditText));
                food.setMagnesium(parseNutrientValue(magnesiumEditText));

                // Important micronutrients
                food.setCalcium(parseNutrientValue(calciumEditText));
                food.setZinc(parseNutrientValue(zincEditText));
                food.setPotassium(parseNutrientValue(potassiumEditText));
                food.setSodium(parseNutrientValue(sodiumEditText));
                food.setVitaminC(parseNutrientValue(vitaminCEditText));
            }

            // Save to database
            saveFoodItem(food);

        } catch (Exception e) {
            Log.e(TAG, "Error creating food item", e);
            Toast.makeText(this, "Error creating food item. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFoodItem(Food food) {
        // Show progress
        createFoodButton.setEnabled(false);
        createFoodButton.setText("Creating...");

        mFirestoreManager.saveFood(food, new FirestoreManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String foodId) {
                Log.d(TAG, "Food item created successfully: " + food.getName());

                runOnUiThread(() -> {
                    Toast.makeText(CreateFoodItemActivity.this,
                            "Food item created successfully!", Toast.LENGTH_SHORT).show();

                    // Set the ID and navigate to AddFoodActivity
                    food.setId(foodId);
                    navigateToAddFood(food);
                });
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error creating food item", error);

                runOnUiThread(() -> {
                    Toast.makeText(CreateFoodItemActivity.this,
                            "Error creating food item: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Re-enable button
                    createFoodButton.setEnabled(true);
                    createFoodButton.setText("Create food item");
                });
            }
        });
    }

    private void navigateToAddFood(Food food) {
        try {
            Intent intent = new Intent(CreateFoodItemActivity.this, AddFoodActivity.class);
            intent.putExtra("client_id", clientId);
            intent.putExtra("client_name", clientName);
            intent.putExtra("selected_date", selectedDate);
            intent.putExtra("selected_food", food);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to AddFoodActivity", e);
            Toast.makeText(this, "Food created but error opening next screen", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }
}