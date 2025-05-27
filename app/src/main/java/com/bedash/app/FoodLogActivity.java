package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoodLogActivity extends BaseActivity {
    private static final String TAG = "FoodLogActivity";

    private TextView dateTextView;
    private ImageButton backButton;
    private ImageButton prevDateButton;
    private ImageButton nextDateButton;
    private ListView foodEntriesListView;
    private Button addNewFoodItemButton;
    private TextView emptyTextView;

    private String clientId;
    private String clientName;
    private FirestoreManager mFirestoreManager;

    private Calendar currentDate;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat displayDateFormat;

    private ArrayList<Map<String, String>> foodEntriesList;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_log);
        setupBase();

        // Initialize Firestore Manager
        mFirestoreManager = FirestoreManager.getInstance();

        // Get client data
        if (getIntent().hasExtra("client_id") && getIntent().hasExtra("client_name")) {
            clientId = getIntent().getStringExtra("client_id");
            clientName = getIntent().getStringExtra("client_name");
        } else {
            Toast.makeText(this, "Error: No client data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize date formatters
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        displayDateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        currentDate = Calendar.getInstance();

        initializeViews();
        setupClickListeners();
        loadFoodEntries();
    }

    private void initializeViews() {
        dateTextView = findViewById(R.id.date_text);
        backButton = findViewById(R.id.back_button);
        prevDateButton = findViewById(R.id.prev_date_button);
        nextDateButton = findViewById(R.id.next_date_button);
        foodEntriesListView = findViewById(R.id.food_entries_list);
        addNewFoodItemButton = findViewById(R.id.add_new_food_item_button);
        emptyTextView = findViewById(R.id.empty_text_view);

        // Initialize list
        foodEntriesList = new ArrayList<>();
        adapter = new SimpleAdapter(
                this,
                foodEntriesList,
                R.layout.food_entry_list_item,
                new String[]{"time", "food_name", "servings", "calories"},
                new int[]{R.id.time_text, R.id.food_name_text, R.id.servings_text, R.id.calories_text}
        );

        foodEntriesListView.setAdapter(adapter);
        foodEntriesListView.setEmptyView(emptyTextView);

        updateDateDisplay();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        prevDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.DAY_OF_MONTH, -1);
                updateDateDisplay();
                loadFoodEntries();
            }
        });

        nextDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadFoodEntries();
            }
        });

        addNewFoodItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodLogActivity.this, FoodItemsActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                intent.putExtra("selected_date", dateFormat.format(currentDate.getTime()));
                startActivity(intent);
            }
        });
    }

    private void updateDateDisplay() {
        String displayDate = displayDateFormat.format(currentDate.getTime());
        dateTextView.setText(displayDate);

        // Disable next button if it's today or future
        Calendar today = Calendar.getInstance();
        nextDateButton.setEnabled(currentDate.before(today) ||
                dateFormat.format(currentDate.getTime()).equals(dateFormat.format(today.getTime())));
    }

    private void loadFoodEntries() {
        String selectedDate = dateFormat.format(currentDate.getTime());

        // Clear current list
        foodEntriesList.clear();

        mFirestoreManager.getFoodEntriesForDate(clientId, selectedDate, new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                double totalCalories = 0;

                // Create a list to hold entries with their timestamps for sorting
                ArrayList<Map<String, Object>> tempEntries = new ArrayList<>();

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    FoodEntry entry = mFirestoreManager.documentToFoodEntry(document);
                    if (entry != null && selectedDate.equals(entry.getDate())) { // Added date check for safety
                        Map<String, Object> entryWithTimestamp = new HashMap<>();
                        entryWithTimestamp.put("entry", entry);
                        entryWithTimestamp.put("timestamp", entry.getTimestamp());
                        tempEntries.add(entryWithTimestamp);
                        totalCalories += entry.getTotalCalories();
                    }
                }

                // Sort by timestamp (most recent first)
                tempEntries.sort((a, b) -> {
                    Long timestampA = (Long) a.get("timestamp");
                    Long timestampB = (Long) b.get("timestamp");
                    return timestampB.compareTo(timestampA); // Descending order
                });

                // Convert to display format
                for (Map<String, Object> entryWithTimestamp : tempEntries) {
                    FoodEntry entry = (FoodEntry) entryWithTimestamp.get("entry");
                    Map<String, String> entryMap = new HashMap<>();
                    entryMap.put("time", entry.getFormattedTime());
                    entryMap.put("food_name", entry.getFoodName());
                    entryMap.put("servings", String.format(Locale.getDefault(), "%.1f servings", entry.getServings()));
                    entryMap.put("calories", String.format(Locale.getDefault(), "%.0f cal", entry.getTotalCalories()));

                    foodEntriesList.add(entryMap);
                }

                adapter.notifyDataSetChanged();

                // Update empty view text
                if (foodEntriesList.isEmpty()) {
                    emptyTextView.setText("No food entries for this date.\nTap 'Add new food item' to get started.");
                }

                Log.d(TAG, "Loaded " + foodEntriesList.size() + " food entries for " + selectedDate);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading food entries: " + error.getMessage());
                Toast.makeText(FoodLogActivity.this,
                        "Error loading food entries: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload entries when returning from other activities
        loadFoodEntries();
    }
}