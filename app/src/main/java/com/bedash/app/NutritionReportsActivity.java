package com.bedash.app;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NutritionReportsActivity extends BaseActivity {
    private static final String TAG = "NutritionReportsActivity";
    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 1001;

    // UI Components
    private ImageButton backButton;
    private TextView clientNameText;
    private TextView reportTypeText;
    private TextView selectedDateText;
    private Button selectDateButton;
    private Button toggleReportButton;
    private Button shareReportButton;
    private Button savePdfButton; // NEW: PDF button

    // Report Content
    private TextView calorieGoalText;
    private TextView calorieActualText;
    private TextView calorieProgressText;
    private PieChart macronutrientChart;
    private BarChart micronutrientChart;
    private TextView detailedNutritionText;

    // Data
    private String clientId;
    private String clientName;
    private Client client;
    private Calendar selectedDate;
    private boolean isWeeklyReport = false; // false = daily, true = weekly
    private FirestoreManager mFirestoreManager;

    // Nutrition data
    private Map<String, Double> nutritionSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_reports);
        setupBase();

        mFirestoreManager = FirestoreManager.getInstance();

        // Get client data
        if (getIntent().hasExtra("client_id") && getIntent().hasExtra("client_name")) {
            clientId = getIntent().getStringExtra("client_id");
            clientName = getIntent().getStringExtra("client_name");
        } else {
            Toast.makeText(this, "Error: Missing client information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedDate = Calendar.getInstance();
        nutritionSummary = new HashMap<>();

        initializeViews();
        setupClickListeners();
        setupCharts();
        loadClientData();
        generateReport();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.back_button);
        clientNameText = findViewById(R.id.client_name_text);
        reportTypeText = findViewById(R.id.report_type_text);
        selectedDateText = findViewById(R.id.selected_date_text);
        selectDateButton = findViewById(R.id.select_date_button);
        toggleReportButton = findViewById(R.id.toggle_report_button);
        shareReportButton = findViewById(R.id.share_report_button);
        savePdfButton = findViewById(R.id.save_pdf_button); // NEW: Initialize PDF button

        calorieGoalText = findViewById(R.id.calorie_goal_text);
        calorieActualText = findViewById(R.id.calorie_actual_text);
        calorieProgressText = findViewById(R.id.calorie_progress_text);
        macronutrientChart = findViewById(R.id.macronutrient_chart);
        micronutrientChart = findViewById(R.id.micronutrient_chart);
        detailedNutritionText = findViewById(R.id.detailed_nutrition_text);

        // Set initial values
        clientNameText.setText("Client: " + clientName);
        updateReportTypeDisplay();
        updateDateDisplay();
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> onBackPressed());

        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        toggleReportButton.setOnClickListener(v -> {
            isWeeklyReport = !isWeeklyReport;
            updateReportTypeDisplay();
            generateReport();
        });

        shareReportButton.setOnClickListener(v -> shareReport());

        // NEW: PDF button click listener
        savePdfButton.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                generatePdfReport();
            } else {
                requestStoragePermission();
            }
        });
    }

    // NEW: Permission handling methods
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Android 10+ uses scoped storage, no permission needed for app-specific directories
            return true;
        } else {
            // For older versions, check WRITE_EXTERNAL_STORAGE permission
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_WRITE_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePdfReport();
            } else {
                Toast.makeText(this, "Storage permission required to save PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // NEW: PDF Generation Method
    private void generatePdfReport() {
        if (nutritionSummary == null || client == null) {
            Toast.makeText(this, "No report data available", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create PDF document
            PdfDocument pdfDocument = new PdfDocument();

            // Create page info
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(612, 792, 1).create(); // 8.5 x 11 inches
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Set up paint for different text styles
            Paint titlePaint = new Paint();
            titlePaint.setTextSize(24);
            titlePaint.setColor(Color.BLACK);
            titlePaint.setFakeBoldText(true);

            Paint headerPaint = new Paint();
            headerPaint.setTextSize(18);
            headerPaint.setColor(Color.BLACK);
            headerPaint.setFakeBoldText(true);

            Paint normalPaint = new Paint();
            normalPaint.setTextSize(14);
            normalPaint.setColor(Color.BLACK);

            Paint smallPaint = new Paint();
            smallPaint.setTextSize(12);
            smallPaint.setColor(Color.GRAY);

            // Starting positions
            float x = 50;
            float y = 80;
            float lineHeight = 20;

            // Title
            String period = isWeeklyReport ? "Weekly" : "Daily";
            canvas.drawText(period + " Nutrition Report", x, y, titlePaint);
            y += lineHeight * 2;

            // Client info
            canvas.drawText("Client: " + clientName, x, y, headerPaint);
            y += lineHeight;
            canvas.drawText("Date: " + selectedDateText.getText().toString(), x, y, normalPaint);
            y += lineHeight * 2;

            // Calorie Summary
            canvas.drawText("CALORIE SUMMARY", x, y, headerPaint);
            y += lineHeight;
            canvas.drawText(calorieGoalText.getText().toString(), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(calorieActualText.getText().toString(), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(calorieProgressText.getText().toString(), x, y, normalPaint);
            y += lineHeight * 2;

            // Macronutrient Summary
            canvas.drawText("MACRONUTRIENTS", x, y, headerPaint);
            y += lineHeight;

            double protein = nutritionSummary.getOrDefault("totalProtein", 0.0);
            double carbs = nutritionSummary.getOrDefault("totalCarbohydrates", 0.0);
            double fat = nutritionSummary.getOrDefault("totalFat", 0.0);
            double fiber = nutritionSummary.getOrDefault("totalFiber", 0.0);
            double sodium = nutritionSummary.getOrDefault("totalSodium", 0.0);

            canvas.drawText(String.format("• Protein: %.1fg", protein), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Carbohydrates: %.1fg", carbs), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("  - Fiber: %.1fg", fiber), x + 20, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Total Fat: %.1fg", fat), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Sodium: %.0fmg", sodium), x, y, normalPaint);
            y += lineHeight * 2;

            // Micronutrient Summary
            canvas.drawText("KEY MICRONUTRIENTS", x, y, headerPaint);
            y += lineHeight;

            double iron = nutritionSummary.getOrDefault("totalIron", 0.0);
            double vitD = nutritionSummary.getOrDefault("totalVitaminD", 0.0);
            double vitB12 = nutritionSummary.getOrDefault("totalVitaminB12", 0.0);
            double folate = nutritionSummary.getOrDefault("totalFolate", 0.0);
            double calcium = nutritionSummary.getOrDefault("totalCalcium", 0.0);
            double vitC = nutritionSummary.getOrDefault("totalVitaminC", 0.0);

            canvas.drawText(String.format("• Iron: %.1fmg", iron), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Vitamin D: %.1fmcg", vitD), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Vitamin B12: %.1fmcg", vitB12), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Folate: %.0fmcg", folate), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Calcium: %.0fmg", calcium), x, y, normalPaint);
            y += lineHeight;
            canvas.drawText(String.format("• Vitamin C: %.1fmg", vitC), x, y, normalPaint);
            y += lineHeight * 3;

            // Footer
            SimpleDateFormat footerDate = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            canvas.drawText("Generated on " + footerDate.format(Calendar.getInstance().getTime()),
                    x, pageInfo.getPageHeight() - 50, smallPaint);
            canvas.drawText("BeDash Health - Nutrition Tracking System",
                    x, pageInfo.getPageHeight() - 30, smallPaint);

            pdfDocument.finishPage(page);

            // Save the PDF
            savePdfToFile(pdfDocument);

        } catch (Exception e) {
            Log.e(TAG, "Error generating PDF", e);
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void savePdfToFile(PdfDocument pdfDocument) {
        try {
            // Create filename
            SimpleDateFormat fileDate = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String timestamp = fileDate.format(Calendar.getInstance().getTime());
            String period = isWeeklyReport ? "Weekly" : "Daily";
            String fileName = clientName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + period + "_Report_" + timestamp + ".pdf";

            File file;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+: Save to app-specific directory
                File documentsDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "NutritionReports");
                if (!documentsDir.exists()) {
                    documentsDir.mkdirs();
                }
                file = new File(documentsDir, fileName);
            } else {
                // Older versions: Save to Downloads
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                file = new File(downloadsDir, fileName);
            }

            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.close();
            pdfDocument.close();

            Toast.makeText(this, "PDF saved: " + file.getName(), Toast.LENGTH_LONG).show();

            // Optional: Open the PDF file
            openPdfFile(file);

        } catch (IOException e) {
            Log.e(TAG, "Error saving PDF file", e);
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPdfFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open PDF"));
        } catch (Exception e) {
            Log.e(TAG, "Error opening PDF file", e);
            // File was saved successfully, just couldn't open it
        }
    }

    // ... [Include all the existing methods from the previous version] ...
    // setupCharts(), loadClientData(), generateReport(), generateDailyReport(),
    // generateWeeklyReport(), updateReportTypeDisplay(), updateDateDisplay(),
    // updateCalorieGoalDisplay(), updateCalorieDisplay(), updateMacronutrientChart(),
    // updateMicronutrientChart(), updateDetailedNutritionText(), showDatePickerDialog(),
    // shareReport(), generateShareableReport()

    private void setupCharts() {
        setupMacronutrientChart();
        setupMicronutrientChart();
    }

    private void setupMacronutrientChart() {
        macronutrientChart.setUsePercentValues(true);
        macronutrientChart.getDescription().setEnabled(false);
        macronutrientChart.setExtraOffsets(5, 10, 5, 5);
        macronutrientChart.setDragDecelerationFrictionCoef(0.95f);
        macronutrientChart.setDrawHoleEnabled(true);
        macronutrientChart.setHoleColor(Color.WHITE);
        macronutrientChart.setHoleRadius(45f);
        macronutrientChart.setDrawCenterText(true);
        macronutrientChart.setCenterText("Macronutrients");
        macronutrientChart.setCenterTextSize(14f);
        macronutrientChart.setRotationAngle(0);
        macronutrientChart.setRotationEnabled(true);
        macronutrientChart.setHighlightPerTapEnabled(true);

        Legend legend = macronutrientChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
    }

    private void setupMicronutrientChart() {
        micronutrientChart.getDescription().setEnabled(false);
        micronutrientChart.setTouchEnabled(true);
        micronutrientChart.setDragEnabled(true);
        micronutrientChart.setScaleEnabled(true);
        micronutrientChart.setDrawGridBackground(false);
        micronutrientChart.setPinchZoom(true);
        micronutrientChart.setBackgroundColor(Color.WHITE);

        // Configure X-axis
        XAxis xAxis = micronutrientChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(6);
        xAxis.setTextSize(10f);

        // Configure Y-axes
        micronutrientChart.getAxisLeft().setDrawGridLines(false);
        micronutrientChart.getAxisLeft().setAxisMinimum(0f);
        micronutrientChart.getAxisRight().setEnabled(false);

        Legend legend = micronutrientChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(11f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void loadClientData() {
        mFirestoreManager.getClient(clientId, new FirestoreManager.DatabaseCallback<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    client = mFirestoreManager.documentToClient(documentSnapshot);
                    runOnUiThread(() -> {
                        if (client != null) {
                            updateCalorieGoalDisplay();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading client data", error);
            }
        });
    }

    private void generateReport() {
        if (isWeeklyReport) {
            generateWeeklyReport();
        } else {
            generateDailyReport();
        }
    }

    private void generateDailyReport() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(selectedDate.getTime());

        mFirestoreManager.getDailyNutritionSummary(clientId, dateStr,
                new FirestoreManager.DatabaseCallback<Map<String, Double>>() {
                    @Override
                    public void onSuccess(Map<String, Double> summary) {
                        nutritionSummary = summary;
                        runOnUiThread(() -> {
                            updateCalorieDisplay();
                            updateMacronutrientChart();
                            updateMicronutrientChart();
                            updateDetailedNutritionText();
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "Error generating daily report", error);
                        runOnUiThread(() -> {
                            Toast.makeText(NutritionReportsActivity.this,
                                    "Error loading daily nutrition data", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void generateWeeklyReport() {
        // Calculate week start (Monday) and end (Sunday)
        Calendar weekStart = (Calendar) selectedDate.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_WEEK, 6);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(weekStart.getTime());
        String endDate = dateFormat.format(weekEnd.getTime());

        mFirestoreManager.getFoodEntriesForDateRange(clientId, startDate, endDate,
                new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        // Calculate weekly totals
                        Map<String, Double> weeklyTotals = new HashMap<>();
                        weeklyTotals.put("totalCalories", 0.0);
                        weeklyTotals.put("totalProtein", 0.0);
                        weeklyTotals.put("totalCarbohydrates", 0.0);
                        weeklyTotals.put("totalFiber", 0.0);
                        weeklyTotals.put("totalSugars", 0.0);
                        weeklyTotals.put("totalFat", 0.0);
                        weeklyTotals.put("totalSaturatedFat", 0.0);
                        weeklyTotals.put("totalSodium", 0.0);
                        weeklyTotals.put("totalIron", 0.0);
                        weeklyTotals.put("totalVitaminD", 0.0);
                        weeklyTotals.put("totalVitaminB12", 0.0);
                        weeklyTotals.put("totalFolate", 0.0);
                        weeklyTotals.put("totalCalcium", 0.0);
                        weeklyTotals.put("totalVitaminC", 0.0);

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            FoodEntry entry = mFirestoreManager.documentToFoodEntry(document);
                            if (entry != null) {
                                weeklyTotals.put("totalCalories",
                                        weeklyTotals.get("totalCalories") + entry.getTotalCalories());
                                weeklyTotals.put("totalProtein",
                                        weeklyTotals.get("totalProtein") + entry.getTotalProtein());
                                weeklyTotals.put("totalCarbohydrates",
                                        weeklyTotals.get("totalCarbohydrates") + entry.getTotalCarbohydrates());
                                weeklyTotals.put("totalFiber",
                                        weeklyTotals.get("totalFiber") + entry.getTotalFiber());
                                weeklyTotals.put("totalSugars",
                                        weeklyTotals.get("totalSugars") + entry.getTotalSugars());
                                weeklyTotals.put("totalFat",
                                        weeklyTotals.get("totalFat") + entry.getTotalFat());
                                weeklyTotals.put("totalSaturatedFat",
                                        weeklyTotals.get("totalSaturatedFat") + entry.getTotalSaturatedFat());
                                weeklyTotals.put("totalSodium",
                                        weeklyTotals.get("totalSodium") + entry.getTotalSodium());
                                weeklyTotals.put("totalIron",
                                        weeklyTotals.get("totalIron") + entry.getTotalIron());
                                weeklyTotals.put("totalVitaminD",
                                        weeklyTotals.get("totalVitaminD") + entry.getTotalVitaminD());
                                weeklyTotals.put("totalVitaminB12",
                                        weeklyTotals.get("totalVitaminB12") + entry.getTotalVitaminB12());
                                weeklyTotals.put("totalFolate",
                                        weeklyTotals.get("totalFolate") + entry.getTotalFolate());
                                weeklyTotals.put("totalCalcium",
                                        weeklyTotals.get("totalCalcium") + entry.getTotalCalcium());
                                weeklyTotals.put("totalVitaminC",
                                        weeklyTotals.get("totalVitaminC") + entry.getTotalVitaminC());
                            }
                        }

                        nutritionSummary = weeklyTotals;
                        runOnUiThread(() -> {
                            updateCalorieDisplay();
                            updateMacronutrientChart();
                            updateMicronutrientChart();
                            updateDetailedNutritionText();
                        });
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "Error generating weekly report", error);
                        runOnUiThread(() -> {
                            Toast.makeText(NutritionReportsActivity.this,
                                    "Error loading weekly nutrition data", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void updateReportTypeDisplay() {
        String reportType = isWeeklyReport ? "Weekly Report" : "Daily Report";
        reportTypeText.setText(reportType);
        toggleReportButton.setText(isWeeklyReport ? "Switch to Daily" : "Switch to Weekly");
        updateDateDisplay();
    }

    private void updateDateDisplay() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
        if (isWeeklyReport) {
            Calendar weekStart = (Calendar) selectedDate.clone();
            weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Calendar weekEnd = (Calendar) weekStart.clone();
            weekEnd.add(Calendar.DAY_OF_WEEK, 6);

            SimpleDateFormat shortFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            selectedDateText.setText("Week of " + shortFormat.format(weekStart.getTime()) +
                    " - " + shortFormat.format(weekEnd.getTime()));
        } else {
            selectedDateText.setText(displayFormat.format(selectedDate.getTime()));
        }
    }

    private void updateCalorieGoalDisplay() {
        if (client != null) {
            int goal = isWeeklyReport ? client.getGoalCalories() * 7 : client.getGoalCalories();
            calorieGoalText.setText(String.format(Locale.getDefault(),
                    "Goal: %,d calories", goal));
        }
    }

    private void updateCalorieDisplay() {
        if (nutritionSummary != null) {
            double actualCalories = nutritionSummary.getOrDefault("totalCalories", 0.0);
            calorieActualText.setText(String.format(Locale.getDefault(),
                    "Actual: %,.0f calories", actualCalories));

            if (client != null) {
                int goal = isWeeklyReport ? client.getGoalCalories() * 7 : client.getGoalCalories();
                double progress = (actualCalories / goal) * 100;
                calorieProgressText.setText(String.format(Locale.getDefault(),
                        "Progress: %.1f%%", progress));

                // Color code the progress
                if (progress >= 90 && progress <= 110) {
                    calorieProgressText.setTextColor(Color.rgb(76, 175, 80)); // Green
                } else if (progress >= 80 && progress <= 120) {
                    calorieProgressText.setTextColor(Color.rgb(255, 193, 7)); // Yellow
                } else {
                    calorieProgressText.setTextColor(Color.rgb(244, 67, 54)); // Red
                }
            }
        }
    }

    private void updateMacronutrientChart() {
        if (nutritionSummary == null) return;

        ArrayList<PieEntry> entries = new ArrayList<>();

        double protein = nutritionSummary.getOrDefault("totalProtein", 0.0);
        double carbs = nutritionSummary.getOrDefault("totalCarbohydrates", 0.0);
        double fat = nutritionSummary.getOrDefault("totalFat", 0.0);

        // Calculate calories from macros (protein: 4 cal/g, carbs: 4 cal/g, fat: 9 cal/g)
        double proteinCal = protein * 4;
        double carbsCal = carbs * 4;
        double fatCal = fat * 9;
        double totalMacroCal = proteinCal + carbsCal + fatCal;

        if (totalMacroCal > 0) {
            entries.add(new PieEntry((float) proteinCal, "Protein"));
            entries.add(new PieEntry((float) carbsCal, "Carbs"));
            entries.add(new PieEntry((float) fatCal, "Fat"));
        } else {
            entries.add(new PieEntry(1f, "No data available"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(4f, 4f));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<>();
        if (totalMacroCal > 0) {
            colors.add(Color.rgb(255, 87, 87));  // Red for protein
            colors.add(Color.rgb(76, 175, 80));  // Green for carbs
            colors.add(Color.rgb(33, 150, 243)); // Blue for fat
        } else {
            colors.add(Color.rgb(224, 224, 224)); // Gray for no data
        }
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        macronutrientChart.setData(data);
        macronutrientChart.invalidate();
    }

    private void updateMicronutrientChart() {
        if (nutritionSummary == null) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        // Key micronutrients with their daily values
        String[] nutrients = {"Iron", "Vit D", "B12", "Folate", "Calcium", "Vit C"};
        String[] keys = {"totalIron", "totalVitaminD", "totalVitaminB12", "totalFolate", "totalCalcium", "totalVitaminC"};
        double[] dailyValues = {18.0, 20.0, 2.4, 400.0, 1000.0, 90.0}; // RDA values

        for (int i = 0; i < nutrients.length; i++) {
            double actual = nutritionSummary.getOrDefault(keys[i], 0.0);
            double targetDV = isWeeklyReport ? dailyValues[i] * 7 : dailyValues[i];
            float percentage = (float) ((actual / targetDV) * 100);

            entries.add(new BarEntry(i, percentage));
            labels.add(nutrients[i]);
        }

        BarDataSet dataSet = new BarDataSet(entries, "% Daily Value");
        dataSet.setColors(Color.rgb(76, 175, 80)); // Green bars
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        micronutrientChart.setData(barData);
        micronutrientChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        micronutrientChart.getXAxis().setLabelCount(labels.size());
        micronutrientChart.invalidate();
    }

    private void updateDetailedNutritionText() {
        if (nutritionSummary == null) return;

        StringBuilder detailed = new StringBuilder();
        String period = isWeeklyReport ? "Weekly" : "Daily";

        detailed.append(String.format("%s Nutrition Summary:\n\n", period));

        // Macronutrients
        detailed.append("MACRONUTRIENTS:\n");
        detailed.append(String.format("• Protein: %.1fg\n", nutritionSummary.getOrDefault("totalProtein", 0.0)));
        detailed.append(String.format("• Carbohydrates: %.1fg\n", nutritionSummary.getOrDefault("totalCarbohydrates", 0.0)));
        detailed.append(String.format("  - Fiber: %.1fg\n", nutritionSummary.getOrDefault("totalFiber", 0.0)));
        detailed.append(String.format("  - Sugars: %.1fg\n", nutritionSummary.getOrDefault("totalSugars", 0.0)));
        detailed.append(String.format("• Total Fat: %.1fg\n", nutritionSummary.getOrDefault("totalFat", 0.0)));
        detailed.append(String.format("  - Saturated: %.1fg\n", nutritionSummary.getOrDefault("totalSaturatedFat", 0.0)));
        detailed.append(String.format("• Sodium: %.0fmg\n\n", nutritionSummary.getOrDefault("totalSodium", 0.0)));

        // Micronutrients
        detailed.append("KEY MICRONUTRIENTS:\n");
        detailed.append(String.format("• Iron: %.1fmg\n", nutritionSummary.getOrDefault("totalIron", 0.0)));
        detailed.append(String.format("• Vitamin D: %.1fmcg\n", nutritionSummary.getOrDefault("totalVitaminD", 0.0)));
        detailed.append(String.format("• Vitamin B12: %.1fmcg\n", nutritionSummary.getOrDefault("totalVitaminB12", 0.0)));
        detailed.append(String.format("• Folate: %.0fmcg\n", nutritionSummary.getOrDefault("totalFolate", 0.0)));
        detailed.append(String.format("• Calcium: %.0fmg\n", nutritionSummary.getOrDefault("totalCalcium", 0.0)));
        detailed.append(String.format("• Vitamin C: %.1fmg", nutritionSummary.getOrDefault("totalVitaminC", 0.0)));

        detailedNutritionText.setText(detailed.toString());
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateDisplay();
                    generateReport();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void shareReport() {
        if (nutritionSummary == null) {
            Toast.makeText(this, "No report data to share", Toast.LENGTH_SHORT).show();
            return;
        }

        String reportText = generateShareableReport();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, clientName + " Nutrition Report");
        shareIntent.putExtra(Intent.EXTRA_TEXT, reportText);
        startActivity(Intent.createChooser(shareIntent, "Share Report"));
    }

    private String generateShareableReport() {
        StringBuilder report = new StringBuilder();
        String period = isWeeklyReport ? "Weekly" : "Daily";

        report.append(String.format("=== %s NUTRITION REPORT ===\n", period.toUpperCase()));
        report.append(String.format("Client: %s\n", clientName));
        report.append(String.format("Date: %s\n\n", selectedDateText.getText().toString()));

        if (client != null) {
            int goal = isWeeklyReport ? client.getGoalCalories() * 7 : client.getGoalCalories();
            double actual = nutritionSummary.getOrDefault("totalCalories", 0.0);
            double progress = (actual / goal) * 100;

            report.append("CALORIE SUMMARY:\n");
            report.append(String.format("Goal: %,d calories\n", goal));
            report.append(String.format("Actual: %,.0f calories\n", actual));
            report.append(String.format("Progress: %.1f%%\n\n", progress));
        }

        report.append(detailedNutritionText.getText().toString());
        report.append("\n\n--- Generated by BeDash Health App ---");

        return report.toString();
    }
}