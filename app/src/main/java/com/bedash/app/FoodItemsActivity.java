package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoodItemsActivity extends BaseActivity {
    private static final String TAG = "FoodItemsActivity";

    private ImageButton backButton;
    private EditText searchEditText;
    private ListView foodItemsListView;
    private Button addNewFoodItemButton;
    private TextView emptyTextView;
    private ProgressBar progressBar;

    private String clientId;
    private String clientName;
    private String selectedDate;
    private FirestoreManager mFirestoreManager;

    private ArrayList<Map<String, String>> foodItemsList;
    private ArrayList<Food> foodObjects; // Keep track of actual Food objects
    private ArrayList<Map<String, String>> allFoodItemsList; // Keep all items for filtering
    private ArrayList<Food> allFoodObjects; // Keep all Food objects for filtering
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_items);
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
        setupSearchFunctionality();
        setupClickListeners();
        loadFoodItems();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        searchEditText = findViewById(R.id.search_edit_text);
        foodItemsListView = findViewById(R.id.food_items_list);
        addNewFoodItemButton = findViewById(R.id.add_new_food_item_button);
        emptyTextView = findViewById(R.id.empty_text_view);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize lists
        foodItemsList = new ArrayList<>();
        foodObjects = new ArrayList<>();
        allFoodItemsList = new ArrayList<>();
        allFoodObjects = new ArrayList<>();

        adapter = new SimpleAdapter(
                this,
                foodItemsList,
                R.layout.client_list_item, // Reusing existing layout
                new String[]{"name", "details"},
                new int[]{R.id.client_name, R.id.client_details}
        );

        foodItemsListView.setAdapter(adapter);
        foodItemsListView.setEmptyView(emptyTextView);
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterFoodItems(query);
            }
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addNewFoodItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodItemsActivity.this, CreateFoodItemActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                intent.putExtra("selected_date", selectedDate);
                startActivity(intent);
            }
        });

        foodItemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < foodObjects.size()) {
                    Food selectedFood = foodObjects.get(position);

                    Intent intent = new Intent(FoodItemsActivity.this, AddFoodActivity.class);
                    intent.putExtra("client_id", clientId);
                    intent.putExtra("client_name", clientName);
                    intent.putExtra("selected_date", selectedDate);
                    intent.putExtra("selected_food", selectedFood);
                    startActivity(intent);
                }
            }
        });
    }

    private void loadFoodItems() {
        showProgress(true);

        // Clear current lists
        foodItemsList.clear();
        foodObjects.clear();
        allFoodItemsList.clear();
        allFoodObjects.clear();

        mFirestoreManager.getAllFoods(new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                showProgress(false);

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Food food = mFirestoreManager.documentToFood(document);
                    if (food != null) {
                        Map<String, String> foodMap = new HashMap<>();
                        foodMap.put("name", food.getName());

                        // Combine category, calories, and serving info into details
                        String details = "";
                        if (food.getCategory() != null && !food.getCategory().isEmpty()) {
                            details += food.getCategory() + " • ";
                        }
                        details += String.format(Locale.getDefault(), "%.0f cal per %s",
                                food.getCaloriesPerServing(), food.getServingSize());

                        foodMap.put("details", details);

                        // Add to both current and all lists
                        foodItemsList.add(foodMap);
                        foodObjects.add(food);
                        allFoodItemsList.add(new HashMap<>(foodMap)); // Create copy
                        allFoodObjects.add(food);
                    }
                }

                // Sort alphabetically
                sortFoodItems();
                adapter.notifyDataSetChanged();

                if (foodItemsList.isEmpty()) {
                    emptyTextView.setText("No food items available.\nTap 'Add new food item' to create one.");
                } else {
                    emptyTextView.setText("No food items match your search.\nTry a different search term or add a new food item.");
                }

                Log.d(TAG, "Loaded " + foodItemsList.size() + " food items");
            }

            @Override
            public void onError(Exception error) {
                showProgress(false);
                Log.e(TAG, "Error loading food items: " + error.getMessage());
                Toast.makeText(FoodItemsActivity.this,
                        "Error loading food items: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                emptyTextView.setText("Error loading food items. Please try again.");
            }
        });
    }

    private void filterFoodItems(String query) {
        foodItemsList.clear();
        foodObjects.clear();

        if (query.isEmpty()) {
            // Show all items if no search query
            foodItemsList.addAll(allFoodItemsList);
            foodObjects.addAll(allFoodObjects);
        } else {
            // Filter items based on search query
            String queryLower = query.toLowerCase();

            for (int i = 0; i < allFoodObjects.size(); i++) {
                Food food = allFoodObjects.get(i);
                String name = food.getName().toLowerCase();
                String category = food.getCategory() != null ? food.getCategory().toLowerCase() : "";

                // Search in name or category
                if (name.contains(queryLower) || category.contains(queryLower)) {
                    foodItemsList.add(allFoodItemsList.get(i));
                    foodObjects.add(food);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Update empty message based on search
        if (foodItemsList.isEmpty()) {
            if (query.isEmpty()) {
                emptyTextView.setText("No food items available.\nTap 'Add new food item' to create one.");
            } else {
                emptyTextView.setText("No food items match \"" + query + "\".\nTry a different search term or add a new food item.");
            }
        }
    }

    private void sortFoodItems() {
        // Create a temporary list of indices
        ArrayList<Integer> indices = new ArrayList<>();
        for (int i = 0; i < foodItemsList.size(); i++) {
            indices.add(i);
        }

        // Sort indices based on food names
        indices.sort((a, b) -> foodItemsList.get(a).get("name").compareToIgnoreCase(foodItemsList.get(b).get("name")));

        // Create new sorted lists
        ArrayList<Map<String, String>> sortedFoodItemsList = new ArrayList<>();
        ArrayList<Food> sortedFoodObjects = new ArrayList<>();

        for (int index : indices) {
            sortedFoodItemsList.add(foodItemsList.get(index));
            sortedFoodObjects.add(foodObjects.get(index));
        }

        // Replace original lists
        foodItemsList.clear();
        foodObjects.clear();
        foodItemsList.addAll(sortedFoodItemsList);
        foodObjects.addAll(sortedFoodObjects);

        // Also sort the "all" lists to maintain consistency
        if (allFoodItemsList.size() == allFoodObjects.size()) {
            ArrayList<Integer> allIndices = new ArrayList<>();
            for (int i = 0; i < allFoodItemsList.size(); i++) {
                allIndices.add(i);
            }

            allIndices.sort((a, b) -> allFoodItemsList.get(a).get("name").compareToIgnoreCase(allFoodItemsList.get(b).get("name")));

            ArrayList<Map<String, String>> sortedAllFoodItemsList = new ArrayList<>();
            ArrayList<Food> sortedAllFoodObjects = new ArrayList<>();

            for (int index : allIndices) {
                sortedAllFoodItemsList.add(allFoodItemsList.get(index));
                sortedAllFoodObjects.add(allFoodObjects.get(index));
            }

            allFoodItemsList.clear();
            allFoodObjects.clear();
            allFoodItemsList.addAll(sortedAllFoodItemsList);
            allFoodObjects.addAll(sortedAllFoodObjects);
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        foodItemsListView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload food items when returning from other activities
//        loadFoodItems();
    }
}