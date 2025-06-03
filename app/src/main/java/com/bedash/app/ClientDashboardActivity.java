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

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.components.Description;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ClientDashboardActivity extends BaseActivity {
    private static final String TAG = "ClientDashboardActivity";

    private TextView clientNameTextView;
    private TextView weeklyTotalTextView;
    private TextView dailyGoalProgressText;
    private TextView[] dayTextViews = new TextView[7];
    private TextView[] calorieTextViews = new TextView[7];
    private Button foodLogButton;
    private ImageButton backButton;

    // Charts
    private PieChart dailyProgressChart;
    private LineChart weeklyTrendChart;
    private BarChart macroChart;

    private String clientId;
    private String clientName;
    private FirestoreManager mFirestoreManager;
    private DatabaseReference mDatabase;

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

        // Initialize Firebase
        mFirestoreManager = FirestoreManager.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
        loadClientGoals();
        loadTodayProgress();
        loadWeeklySummary();
    }

    private void initializeViews() {
        clientNameTextView = findViewById(R.id.client_name_text);
        weeklyTotalTextView = findViewById(R.id.weekly_total_text);
        dailyGoalProgressText = findViewById(R.id.daily_goal_progress_text);
        foodLogButton = findViewById(R.id.food_log_button);
        backButton = findViewById(R.id.back_button);

        // Initialize charts
        dailyProgressChart = findViewById(R.id.daily_progress_chart);
        weeklyTrendChart = findViewById(R.id.weekly_trend_chart);
        // Remove macro chart since we don't have macro data
        // macroChart = findViewById(R.id.macro_chart);

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

        setupCharts();
    }

    private void setupCharts() {
        // Setup Daily Progress Ring Chart
        setupDailyProgressChart();

        // Setup Weekly Trend Line Chart
        setupWeeklyTrendChart();

        // Note: Removed macro chart since your FoodEntry doesn't have macro data
    }

    private void setupDailyProgressChart() {
        dailyProgressChart.setUsePercentValues(true);
        dailyProgressChart.getDescription().setEnabled(false);
        dailyProgressChart.setExtraOffsets(5, 10, 5, 5);

        dailyProgressChart.setDragDecelerationFrictionCoef(0.95f);
        dailyProgressChart.setDrawHoleEnabled(true);
        dailyProgressChart.setHoleColor(Color.WHITE);
        dailyProgressChart.setHoleRadius(58f);
        dailyProgressChart.setDrawCenterText(true);
        dailyProgressChart.setCenterText("Daily\nProgress");
        dailyProgressChart.setCenterTextSize(16f);
        dailyProgressChart.setCenterTextColor(Color.BLACK);

        dailyProgressChart.setRotationAngle(0);
        dailyProgressChart.setRotationEnabled(true);
        dailyProgressChart.setHighlightPerTapEnabled(true);

        Legend legend = dailyProgressChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private void setupWeeklyTrendChart() {
        weeklyTrendChart.getDescription().setEnabled(false);
        weeklyTrendChart.setTouchEnabled(true);
        weeklyTrendChart.setDragEnabled(true);
        weeklyTrendChart.setScaleEnabled(true);
        weeklyTrendChart.setDrawGridBackground(false);
        weeklyTrendChart.setPinchZoom(true);
        weeklyTrendChart.setBackgroundColor(Color.WHITE);

        weeklyTrendChart.getAxisLeft().setTextColor(Color.BLACK);
        weeklyTrendChart.getAxisRight().setEnabled(false);
        weeklyTrendChart.getXAxis().setTextColor(Color.BLACK);

        Legend legend = weeklyTrendChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(11f);
        legend.setTextColor(Color.BLACK);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    // Remove setupMacroChart() method since we're not using macro data

    private void loadClientGoals() {
        mDatabase.child("clients").child(clientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("goalCalories").exists()) {
                        dailyCalorieGoal = dataSnapshot.child("goalCalories").getValue(Integer.class);
                    }

                    // Update charts with new goals
                    updateDailyProgressChart();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading client goals: " + databaseError.getMessage());
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

        // Colors
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
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        dailyProgressChart.setData(data);
        dailyProgressChart.highlightValues(null);
        dailyProgressChart.invalidate();
    }

    // Remove the macro chart methods since we don't have macro data
    // updateMacroChart() and setupMacroChart() are no longer needed

    private void updateWeeklyTrendChart() {
        // This will be populated as we load weekly data
        ArrayList<Entry> entries = new ArrayList<>();

        // Add sample data for now - you can populate this with actual weekly data
        for (int i = 0; i < 7; i++) {
            // This should be replaced with actual calorie data for each day
            entries.add(new Entry(i, 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Weekly Calories");
        dataSet.setColor(Color.rgb(33, 150, 243));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(9f);

        LineData lineData = new LineData(dataSet);
        weeklyTrendChart.setData(lineData);
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
        ArrayList<Entry> weeklyEntries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());
            loadDayCalories(date, i, weeklyEntries);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void loadDayCalories(String date, final int dayIndex, ArrayList<Entry> weeklyEntries) {
        mFirestoreManager.getFoodEntriesForDate(clientId, date, new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                double totalCalories = 0;

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    FoodEntry entry = mFirestoreManager.documentToFoodEntry(document);
                    if (entry != null && date.equals(entry.getDate())) {
                        totalCalories += entry.getTotalCalories();
                    }
                }

                final double finalTotalCalories = totalCalories;

                runOnUiThread(() -> {
                    calorieTextViews[dayIndex].setText(String.format(Locale.getDefault(), "%.0f cal", finalTotalCalories));

                    // Add to weekly chart data
                    weeklyEntries.add(new Entry(dayIndex, (float) finalTotalCalories));

                    // Update weekly chart when all days are loaded
                    if (weeklyEntries.size() == 7) {
                        updateWeeklyTrendChart(weeklyEntries);
                    }

                    updateWeeklyTotal();
                });
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading day calories: " + error.getMessage());
            }
        });
    }

    private void updateWeeklyTrendChart(ArrayList<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Weekly Calories");
        dataSet.setColor(Color.rgb(33, 150, 243));
        dataSet.setLineWidth(3f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.rgb(33, 150, 243));
        dataSet.setCircleRadius(4f);

        // Add goal line
        ArrayList<Entry> goalEntries = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            goalEntries.add(new Entry(i, dailyCalorieGoal));
        }

        LineDataSet goalSet = new LineDataSet(goalEntries, "Daily Goal");
        goalSet.setColor(Color.rgb(255, 87, 87));
        goalSet.setLineWidth(2f);
        goalSet.setDrawCircles(false);
        goalSet.enableDashedLine(10f, 5f, 0f);

        LineData lineData = new LineData(dataSet, goalSet);
        weeklyTrendChart.setData(lineData);
        weeklyTrendChart.invalidate();
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
        loadTodayProgress();
        loadWeeklySummary();
    }
}