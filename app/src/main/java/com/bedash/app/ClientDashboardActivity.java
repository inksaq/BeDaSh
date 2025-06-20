package com.bedash.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.utils.MPPointF;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ClientDashboardActivity extends BaseActivity {
    private static final String TAG = "ClientDashboardActivity";

    private TextView clientNameTextView;
    private TextView weeklyTotalTextView;
    private TextView dailyGoalProgressText;
    private TextView[] dayTextViews = new TextView[7];
    private TextView[] calorieTextViews = new TextView[7];
    private Button foodLogButton;
    private Button topfoodLogButton;
    private ImageButton backButton;
    private Button nutritionReportsButton;

    // Charts
    private PieChart dailyProgressChart;
    private LineChart weeklyTrendChart;

    private String clientId;
    private String clientName;
    private FirestoreManager mFirestoreManager;

    // Client goals
    private int dailyCalorieGoal = 2000;

    // Current day totals
    private double todayCalories = 0;

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
        setupCharts();
        loadClientGoals();
        loadTodayProgress();
        loadWeeklySummary();
    }

    private void initializeViews() {
        clientNameTextView = findViewById(R.id.client_name_text);
        weeklyTotalTextView = findViewById(R.id.weekly_total_text);
        dailyGoalProgressText = findViewById(R.id.daily_goal_progress_text);
        foodLogButton = findViewById(R.id.food_log_button);
        nutritionReportsButton = findViewById(R.id.nutrition_reports_button);
        topfoodLogButton = findViewById(R.id.top_food_log_button);
        backButton = findViewById(R.id.back_button);
        topfoodLogButton = findViewById(R.id.top_food_log_button);

        // Initialize charts
        dailyProgressChart = findViewById(R.id.daily_progress_chart);
        weeklyTrendChart = findViewById(R.id.weekly_trend_chart);

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

    private void setupCharts() {
        setupDailyProgressChart();
        setupWeeklyTrendChart();
    }

    private void setupDailyProgressChart() {
        dailyProgressChart.setUsePercentValues(false);
        dailyProgressChart.getDescription().setEnabled(false);
        dailyProgressChart.setExtraOffsets(5, 5, 5, 50); // Minimal side offsets, more space at bottom

        dailyProgressChart.setDragDecelerationFrictionCoef(0.95f);
        dailyProgressChart.setDrawHoleEnabled(true);
        dailyProgressChart.setHoleColor(Color.WHITE);
        dailyProgressChart.setHoleRadius(50f); // Slightly bigger hole for better proportions
        dailyProgressChart.setDrawCenterText(false);

        dailyProgressChart.setRotationAngle(0);
        dailyProgressChart.setRotationEnabled(true);
        dailyProgressChart.setHighlightPerTapEnabled(true);

        Legend legend = dailyProgressChart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(15f);
        legend.setYEntrySpace(5f);
        legend.setYOffset(15f); // More space from bottom for legend
        legend.setXOffset(0f);
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);
    }

    private void setupWeeklyTrendChart() {
        weeklyTrendChart.getDescription().setEnabled(false);
        weeklyTrendChart.setTouchEnabled(true);
        weeklyTrendChart.setDragEnabled(true);
        weeklyTrendChart.setScaleEnabled(true);
        weeklyTrendChart.setDrawGridBackground(false);
        weeklyTrendChart.setPinchZoom(true);
        weeklyTrendChart.setBackgroundColor(Color.WHITE);

        if (weeklyTrendChart.getAxisLeft() != null) {
            weeklyTrendChart.getAxisLeft().setTextColor(Color.BLACK);
        }
        if (weeklyTrendChart.getAxisRight() != null) {
            weeklyTrendChart.getAxisRight().setEnabled(false);
        }
        if (weeklyTrendChart.getXAxis() != null) {
            weeklyTrendChart.getXAxis().setTextColor(Color.BLACK);
        }

        Legend legend = weeklyTrendChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }



    private void loadClientGoals() {
        mFirestoreManager.getClient(clientId, new FirestoreManager.DatabaseCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Client client = mFirestoreManager.documentToClient(documentSnapshot);
                    if (client != null) {
                        dailyCalorieGoal = client.getGoalCalories();
                        Log.d(TAG, "Loaded goal calories from Firestore: " + dailyCalorieGoal);

                        runOnUiThread(() -> {
                            updateDailyProgressChart();
                            updateDailyProgressText();
                        });
                    } else {
                        Log.w(TAG, "Could not convert document to Client object");
                    }
                } else {
                    Log.w(TAG, "Client document does not exist: " + clientId);
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading client goals from Firestore: " + error.getMessage());
            }
        });
    }

    private void loadTodayProgress() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(Calendar.getInstance().getTime());

        mFirestoreManager.getFoodEntriesForDate(clientId, today, new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                todayCalories = 0;

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    FoodEntry entry = mFirestoreManager.documentToFoodEntry(document);
                    if (entry != null) {
                        todayCalories += entry.getTotalCalories();
                    }
                }

                runOnUiThread(() -> {
                    updateDailyProgressChart();
                    updateDailyProgressText();
                });
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading today's progress: " + error.getMessage());
            }
        });
    }

    private void updateDailyProgressChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (dailyCalorieGoal <= 0) {
            dailyCalorieGoal = 2000;
        }

        double progress = Math.min((todayCalories / dailyCalorieGoal) * 100, 100);
        double remaining = 100 - progress;

        entries.add(new PieEntry((float) progress, "Consumed"));
        if (remaining > 0) {
            entries.add(new PieEntry((float) remaining, "Remaining"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Daily Goal Progress");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(4f, 4f));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        if (progress >= 100) {
            colors.add(Color.rgb(255, 87, 87)); // Red for over goal
        } else if (progress >= 80) {
            colors.add(Color.rgb(76, 175, 80)); // Green for near goal
        } else if (progress >= 50) {
            colors.add(Color.rgb(255, 193, 7)); // Yellow for halfway
        } else {
            colors.add(Color.rgb(33, 150, 243)); // Blue for low progress
        }
        colors.add(Color.rgb(224, 224, 224)); // Light gray for remaining

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        dailyProgressChart.setData(data);

        Legend legend = dailyProgressChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);

        dailyProgressChart.highlightValues(null);
        dailyProgressChart.invalidate();
    }

    private void updateWeeklyTrendChart(ArrayList<Entry> entries) {
        // Always create entries for all 7 days, even if some are empty
        if (entries.size() < 7) {
            // Pad with zeros if we don't have all days yet
            while (entries.size() < 7) {
                entries.add(new Entry(entries.size(), 0f));
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Calories");
        dataSet.setColor(Color.rgb(33, 150, 243));
        dataSet.setLineWidth(3f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.rgb(33, 150, 243));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);

        // Create goal line
        ArrayList<Entry> goalEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            goalEntries.add(new Entry(i, dailyCalorieGoal));
        }

        LineDataSet goalSet = new LineDataSet(goalEntries, "Daily Goal");
        goalSet.setColor(Color.rgb(255, 87, 87));
        goalSet.setLineWidth(2f);
        goalSet.setDrawCircles(false);
        goalSet.setDrawValues(false);
        goalSet.enableDashedLine(10f, 5f, 0f);

        LineData lineData = new LineData(dataSet, goalSet);
        weeklyTrendChart.setData(lineData);

        // Configure X-axis labels
        if (weeklyTrendChart.getXAxis() != null) {
            weeklyTrendChart.getXAxis().setGranularity(1f);
            weeklyTrendChart.getXAxis().setLabelCount(7, true);
        }

        weeklyTrendChart.invalidate();
    }

    private void updateDailyProgressText() {
        String progressText = String.format(Locale.getDefault(),
                "Today: %.0f / %d calories (%.1f%%)",
                todayCalories,
                dailyCalorieGoal,
                (todayCalories / dailyCalorieGoal) * 100);
        dailyGoalProgressText.setText(progressText);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        View.OnClickListener foodLogClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, FoodLogActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                startActivity(intent);
            }

        });
        topfoodLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, FoodLogActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                startActivity(intent);
            }
        });
        nutritionReportsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, NutritionReportsActivity.class);
                intent.putExtra("client_id", clientId);
                intent.putExtra("client_name", clientName);
                startActivity(intent);
            }
        });

        };
        foodLogButton.setOnClickListener(foodLogClickListener);
        topfoodLogButton.setOnClickListener(foodLogClickListener);
    }

    private void loadWeeklySummary() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Initialize chart with empty data
        updateWeeklyTrendChart(new ArrayList<>());

        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());
            final int dayIndex = i;

            mFirestoreManager.getFoodEntriesForDate(clientId, date, new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot querySnapshot) {
                    double totalCalories = querySnapshot.getDocuments().stream().map(document -> mFirestoreManager.documentToFoodEntry(document)).filter(Objects::nonNull).mapToDouble(FoodEntry::getTotalCalories).sum();

                    runOnUiThread(() -> {
                        // Update the daily display
                        calorieTextViews[dayIndex].setText(String.format(Locale.getDefault(), "%.0f cal", totalCalories));

                        // Update chart immediately with current data
                        updateWeeklyChartProgressive();
                        updateWeeklyTotal();
                    });
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error loading day calories for day " + dayIndex + ": " + error.getMessage());
                    runOnUiThread(() -> {
                        calorieTextViews[dayIndex].setText("0 cal");
                        updateWeeklyChartProgressive();
                        updateWeeklyTotal();
                    });
                }
            });

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void updateWeeklyChartProgressive() {
        ArrayList<Entry> weeklyEntries = new ArrayList<>();

        // Get current data from the TextViews
        for (int i = 0; i < 7; i++) {
            String text = calorieTextViews[i].getText().toString();
            float calories = 0f;

            if (text.contains("cal")) {
                try {
                    String numberPart = text.replace(" cal", "");
                    calories = Float.parseFloat(numberPart);
                } catch (NumberFormatException e) {
                    calories = 0f;
                }
            }

            weeklyEntries.add(new Entry(i, calories));
        }

        updateWeeklyTrendChart(weeklyEntries);
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
        loadTodayProgress();
        loadWeeklySummary();
    }
}