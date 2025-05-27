package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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
    private ListView foodItemsListView;
    private Button addNewFoodItemButton;
    private TextView emptyTextView;

    private String clientId;
    private String clientName;
    private String selectedDate;
    private FirestoreManager mFirestoreManager;

    private ArrayList<Map<String, String>> foodItemsList;
    private ArrayList<Food> foodObjects; // Keep track of actual Food objects
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
        setupClickListeners();
        loadFoodItems();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        foodItemsListView = findViewById(R.id.food_items_list);
        addNewFoodItemButton = findViewById(R.id.add_new_food_item_button);
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

        foodItemsListView.setAdapter(adapter);
        foodItemsListView.setEmptyView(emptyTextView);
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
        // Clear current lists
        foodItemsList.clear();
        foodObjects.clear();

        mFirestoreManager.getAllFoods(new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Food food = mFirestoreManager.documentToFood(document);
                    if (food != null) {
                        Map<String, String> foodMap = new HashMap<>();
                        foodMap.put("name", food.getName());
                        foodMap.put("category", food.getCategory());
                        foodMap.put("calories", String.format(Locale.getDefault(), "%.0f cal", food.getCaloriesPerServing()));
                        foodMap.put("serving", "per " + food.getServingSize());

                        foodItemsList.add(foodMap);
                        foodObjects.add(food);
                    }
                }

                adapter.notifyDataSetChanged();

                if (foodItemsList.isEmpty()) {
                    emptyTextView.setText("No food items available.\nTap 'Add new food item' to create one.");
                }

                Log.d(TAG, "Loaded " + foodItemsList.size() + " food items");
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading food items: " + error.getMessage());
                Toast.makeText(FoodItemsActivity.this,
                        "Error loading food items: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload food items when returning from other activities
        loadFoodItems();
    }
}