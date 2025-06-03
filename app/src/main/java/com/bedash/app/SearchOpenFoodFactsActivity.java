package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchOpenFoodFactsActivity extends BaseActivity {
    private static final String TAG = "SearchOpenFoodFacts";

    private ImageButton backButton;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ListView resultsListView;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private String clientId;
    private String clientName;
    private String selectedDate;

    private OpenFoodFactsAdapter openFoodFactsAdapter;
    private ArrayList<Map<String, String>> foodItemsList;
    private ArrayList<Food> foodObjects;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_open_food_facts);
        setupBase();

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

        // Initialize OpenFoodFacts adapter
        openFoodFactsAdapter = new OpenFoodFactsAdapter(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        resultsListView = findViewById(R.id.results_list_view);
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.empty_text_view);

        // Initialize lists
        foodItemsList = new ArrayList<>();
        foodObjects = new ArrayList<>();

        adapter = new SimpleAdapter(
                this,
                foodItemsList,
                R.layout.food_item_list_item,
                new String[]{"name", "category", "calories", "serving"},
                new int[]{R.id.food_name_text, R.id.food_category_text, R.id.food_calories_text, R.id.food_serving_text}
        );

        resultsListView.setAdapter(adapter);
        resultsListView.setEmptyView(emptyTextView);

        // Initially hide progress bar
        progressBar.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        // Also search when Enter key is pressed
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Enable search button only if there's text
                searchButton.setEnabled(!s.toString().trim().isEmpty());
            }
        });

        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < foodObjects.size()) {
                    Food selectedFood = foodObjects.get(position);
                    showFoodDetails(selectedFood);
                }
            }
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            searchEditText.setError("Please enter a search term");
            return;
        }

        // Clear previous results
        foodItemsList.clear();
        foodObjects.clear();
        adapter.notifyDataSetChanged();

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setText("Searching...");

        // Perform search
        openFoodFactsAdapter.searchFoods(query, new OpenFoodFactsAdapter.SearchCallback() {
            @Override
            public void onSearchSuccess(List<Food> foods) {
                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                // Update lists
                for (Food food : foods) {
                    Map<String, String> foodMap = new HashMap<>();
                    foodMap.put("name", food.getName());
                    foodMap.put("category", food.getCategory());
                    foodMap.put("calories", String.format(Locale.getDefault(), "%.0f cal", food.getCaloriesPerServing()));
                    foodMap.put("serving", "per " + food.getServingSize());

                    foodItemsList.add(foodMap);
                    foodObjects.add(food);
                }

                adapter.notifyDataSetChanged();

                if (foodItemsList.isEmpty()) {
                    emptyTextView.setText("No results found for \"" + query + "\"");
                }

                Log.d(TAG, "Found " + foodItemsList.size() + " results for query: " + query);
            }

            @Override
            public void onSearchError(String errorMessage) {
                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                // Show error
                emptyTextView.setText("Error: " + errorMessage);
                Log.e(TAG, "Search error: " + errorMessage);
                Toast.makeText(SearchOpenFoodFactsActivity.this,
                        "Search error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFoodDetails(final Food food) {
        // Show a dialog to confirm adding this food to the database
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Food to Database")
                .setMessage("Do you want to add \"" + food.getName() + "\" to your food database?")
                .setPositiveButton("Add", (dialog, which) -> {
                    // Save to Firestore
                    saveToFirestore(food);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void saveToFirestore(final Food food) {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Save to Firestore
        openFoodFactsAdapter.saveToFirestore(food, new FirestoreManager.DatabaseCallback<String>() {
            @Override
            public void onSuccess(String foodId) {
                // Hide progress
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "Food saved to Firestore: " + food.getName() + " with ID: " + foodId);
                Toast.makeText(SearchOpenFoodFactsActivity.this,
                        "Food added to database!", Toast.LENGTH_SHORT).show();

                // Update the food object with the new ID
                food.setId(foodId);

                // Navigate to AddFoodActivity with the saved food
                Intent intent = new Intent(SearchOpenFoodFactsActivity.this, AddFoodActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                intent.putExtra("selected_date", selectedDate);
                intent.putExtra("selected_food", food);
                startActivity(intent);
                finish(); // Close this activity
            }

            @Override
            public void onError(Exception error) {
                // Hide progress
                progressBar.setVisibility(View.GONE);

                Log.e(TAG, "Error saving food to Firestore", error);
                Toast.makeText(SearchOpenFoodFactsActivity.this,
                        "Error adding food: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}