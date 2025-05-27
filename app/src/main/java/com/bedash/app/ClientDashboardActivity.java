package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClientDashboardActivity extends BaseActivity {
    private static final String TAG = "ClientDashboardActivity";

    private TextView clientNameTextView;
    private TextView weeklyTotalTextView;
    private TextView[] dayTextViews = new TextView[7];
    private TextView[] calorieTextViews = new TextView[7];
    private Button foodLogButton;
    private ImageButton backButton;

    private String clientId;
    private String clientName;
    private FirestoreManager mFirestoreManager;

    // Days of the week
    private String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);
        setupBase();

        // Initialize Firestore Manager
        mFirestoreManager = FirestoreManager.getInstance();

        // Get client data passed from previous activity
        if (getIntent().hasExtra("client_id") && getIntent().hasExtra("client_name")) {
            clientId = getIntent().getStringExtra("client_id");
            clientName = getIntent().getStringExtra("client_name");
        } else {
            Toast.makeText(this, "Error: No client data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupClickListeners();
        loadWeeklySummary();
    }

    private void initializeViews() {
        clientNameTextView = findViewById(R.id.client_name_text);
        weeklyTotalTextView = findViewById(R.id.weekly_total_text);
        foodLogButton = findViewById(R.id.food_log_button);
        backButton = findViewById(R.id.back_button);

        // Initialize day and calorie TextViews
        dayTextViews[0] = findViewById(R.id.monday_text);
        dayTextViews[1] = findViewById(R.id.tuesday_text);
        dayTextViews[2] = findViewById(R.id.wednesday_text);
        dayTextViews[3] = findViewById(R.id.thursday_text);
        dayTextViews[4] = findViewById(R.id.friday_text);
        dayTextViews[5] = findViewById(R.id.saturday_text);
        dayTextViews[6] = findViewById(R.id.sunday_text);

        calorieTextViews[0] = findViewById(R.id.monday_calories);
        calorieTextViews[1] = findViewById(R.id.tuesday_calories);
        calorieTextViews[2] = findViewById(R.id.wednesday_calories);
        calorieTextViews[3] = findViewById(R.id.thursday_calories);
        calorieTextViews[4] = findViewById(R.id.friday_calories);
        calorieTextViews[5] = findViewById(R.id.saturday_calories);
        calorieTextViews[6] = findViewById(R.id.sunday_calories);

        // Set client name
        clientNameTextView.setText("Client: " + clientName);

        // Set day names
        for (int i = 0; i < dayNames.length; i++) {
            dayTextViews[i].setText(dayNames[i]);
            calorieTextViews[i].setText("0 cal");
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        foodLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, FoodLogActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                startActivity(intent);
            }
        });
    }

    private void loadWeeklySummary() {
        // Get the current week's dates
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());
            loadDayCalories(date, i);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadDayCalories(String date, final int dayIndex) {
        mFirestoreManager.getFoodEntriesForDate(clientId, date, new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                double totalCalories = 0;

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    FoodEntry entry = mFirestoreManager.documentToFoodEntry(document);
                    if (entry != null && date.equals(entry.getDate())) { // Added date check for safety
                        totalCalories += entry.getTotalCalories();
                    }
                }

                // Make totalCalories effectively final for lambda
                final double finalTotalCalories = totalCalories;

                // Update UI on main thread
                runOnUiThread(() -> {
                    calorieTextViews[dayIndex].setText(String.format(Locale.getDefault(), "%.0f cal", finalTotalCalories));
                    updateWeeklyTotal();
                });
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading day calories: " + error.getMessage());
            }
        });
    }

    private void updateWeeklyTotal() {
        double weeklyTotal = 0;

        for (TextView calorieView : calorieTextViews) {
            String text = calorieView.getText().toString();
            if (text.contains("cal")) {
                try {
                    String numberPart = text.replace(" cal", "");
                    weeklyTotal += Double.parseDouble(numberPart);
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        }

        weeklyTotalTextView.setText(String.format(Locale.getDefault(), "Weekly total: %.0f", weeklyTotal));
    }

    @Override
    protected void handleHomeClick() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from other activities
        loadWeeklySummary();
    }
}