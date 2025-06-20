package com.bedash.app;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodEntry implements Serializable {
    // Basic fields
    private String id;
    private String clientId;
    private String foodId;
    private String foodName;
    private double servings;
    private double totalCalories;
    private long timestamp;
    private String date; // Format: YYYY-MM-DD for easy querying

    // Detailed nutrition tracking per entry (calculated from servings * food values)
    private double totalProtein = 0.0;
    private double totalCarbohydrates = 0.0;
    private double totalFiber = 0.0;
    private double totalSugars = 0.0;
    private double totalFat = 0.0;
    private double totalSaturatedFat = 0.0;
    private double totalSodium = 0.0;

    // Key micronutrients for daily tracking
    private double totalIron = 0.0;        // mg
    private double totalVitaminD = 0.0;    // mcg
    private double totalVitaminB12 = 0.0;  // mcg
    private double totalFolate = 0.0;      // mcg
    private double totalCalcium = 0.0;     // mg
    private double totalVitaminC = 0.0;    // mg

    // Additional fields for meal tracking
    private String mealCategory; // breakfast, lunch, dinner, snack
    private String customTime;   // custom time string if needed

    // Default constructor for Firebase
    public FoodEntry() {}

    // Basic constructor (for backward compatibility)
    public FoodEntry(String clientId, String foodId, String foodName, double servings, double caloriesPerServing) {
        this.clientId = clientId;
        this.foodId = foodId;
        this.foodName = foodName;
        this.servings = servings;
        this.totalCalories = servings * caloriesPerServing;
        this.timestamp = System.currentTimeMillis();

        // Set date in YYYY-MM-DD format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.date = dateFormat.format(new Date(timestamp));
    }

    // Enhanced constructor with complete nutrition information
    public FoodEntry(String clientId, Food food, double servings) {
        this.clientId = clientId;
        this.foodId = food.getId();
        this.foodName = food.getName();
        this.servings = servings;
        this.timestamp = System.currentTimeMillis();

        // Set date in YYYY-MM-DD format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.date = dateFormat.format(new Date(timestamp));

        // Calculate total nutrition based on servings
        calculateNutritionFromFood(food, servings);
    }

    /**
     * Calculate total nutrition values based on food nutrition and serving amount
     */
    private void calculateNutritionFromFood(Food food, double servings) {
        this.totalCalories = servings * food.getCaloriesPerServing();

        // Macronutrients
        this.totalProtein = servings * food.getProtein();
        this.totalCarbohydrates = servings * food.getTotalCarbohydrates();
        this.totalFiber = servings * food.getDietaryFiber();
        this.totalSugars = servings * food.getTotalSugars();
        this.totalFat = servings * food.getTotalFat();
        this.totalSaturatedFat = servings * food.getSaturatedFat();
        this.totalSodium = servings * food.getSodium();

        // Key micronutrients
        this.totalIron = servings * food.getIron();
        this.totalVitaminD = servings * food.getVitaminD();
        this.totalVitaminB12 = servings * food.getVitaminB12();
        this.totalFolate = servings * food.getFolate();
        this.totalCalcium = servings * food.getCalcium();
        this.totalVitaminC = servings * food.getVitaminC();
    }

    // Basic getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public double getServings() {
        return servings;
    }

    public void setServings(double servings) {
        this.servings = servings;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Nutrition getters and setters
    public double getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(double totalProtein) {
        this.totalProtein = totalProtein;
    }

    public double getTotalCarbohydrates() {
        return totalCarbohydrates;
    }

    public void setTotalCarbohydrates(double totalCarbohydrates) {
        this.totalCarbohydrates = totalCarbohydrates;
    }

    public double getTotalFiber() {
        return totalFiber;
    }

    public void setTotalFiber(double totalFiber) {
        this.totalFiber = totalFiber;
    }

    public double getTotalSugars() {
        return totalSugars;
    }

    public void setTotalSugars(double totalSugars) {
        this.totalSugars = totalSugars;
    }

    public double getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(double totalFat) {
        this.totalFat = totalFat;
    }

    public double getTotalSaturatedFat() {
        return totalSaturatedFat;
    }

    public void setTotalSaturatedFat(double totalSaturatedFat) {
        this.totalSaturatedFat = totalSaturatedFat;
    }

    public double getTotalSodium() {
        return totalSodium;
    }

    public void setTotalSodium(double totalSodium) {
        this.totalSodium = totalSodium;
    }

    public double getTotalIron() {
        return totalIron;
    }

    public void setTotalIron(double totalIron) {
        this.totalIron = totalIron;
    }

    public double getTotalVitaminD() {
        return totalVitaminD;
    }

    public void setTotalVitaminD(double totalVitaminD) {
        this.totalVitaminD = totalVitaminD;
    }

    public double getTotalVitaminB12() {
        return totalVitaminB12;
    }

    public void setTotalVitaminB12(double totalVitaminB12) {
        this.totalVitaminB12 = totalVitaminB12;
    }

    public double getTotalFolate() {
        return totalFolate;
    }

    public void setTotalFolate(double totalFolate) {
        this.totalFolate = totalFolate;
    }

    public double getTotalCalcium() {
        return totalCalcium;
    }

    public void setTotalCalcium(double totalCalcium) {
        this.totalCalcium = totalCalcium;
    }

    public double getTotalVitaminC() {
        return totalVitaminC;
    }

    public void setTotalVitaminC(double totalVitaminC) {
        this.totalVitaminC = totalVitaminC;
    }

    // Meal category getters and setters
    public String getMealCategory() {
        return mealCategory;
    }

    public void setMealCategory(String mealCategory) {
        this.mealCategory = mealCategory;
    }

    public String getCustomTime() {
        return customTime;
    }

    public void setCustomTime(String customTime) {
        this.customTime = customTime;
    }

    // Helper methods
    public String getFormattedTime() {
        if (customTime != null && !customTime.isEmpty()) {
            return customTime;
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }

    public double getNetCarbs() {
        return Math.max(0, totalCarbohydrates - totalFiber);
    }

    /**
     * Get percentage of daily value for key nutrients (based on general adult recommendations)
     */
    public double getProteinDVPercent() {
        return (totalProtein / 50.0) * 100; // Based on 50g daily value
    }

    public double getFiberDVPercent() {
        return (totalFiber / 25.0) * 100; // Based on 25g daily value
    }

    public double getSodiumDVPercent() {
        return (totalSodium / 2300.0) * 100; // Based on 2300mg daily limit
    }

    public double getIronDVPercent() {
        return (totalIron / 18.0) * 100; // Based on 18mg daily value
    }

    public double getVitaminDDVPercent() {
        return (totalVitaminD / 20.0) * 100; // Based on 20mcg (800 IU) daily value
    }

    public double getCalciumDVPercent() {
        return (totalCalcium / 1000.0) * 100; // Based on 1000mg daily value
    }

    public double getVitaminCDVPercent() {
        return (totalVitaminC / 90.0) * 100; // Based on 90mg daily value
    }

    /**
     * Clinical assessment flags for healthcare monitoring
     */
    public boolean isHighSodium() {
        return totalSodium > 480; // >20% DV in single food entry
    }

    public boolean isGoodFiberSource() {
        return totalFiber >= 2.5; // ≥10% DV in single food entry
    }

    public boolean isHighProtein() {
        return totalProtein >= 10; // ≥20% DV in single food entry
    }

    /**
     * Nutrition summary for healthcare professionals
     */
    public String getNutritionSummary() {
        return String.format(Locale.getDefault(),
                "%s (%.1f servings):\nCalories: %.0f | Protein: %.1fg | Carbs: %.1fg | Fat: %.1fg\nFiber: %.1fg | Sodium: %.0fmg",
                foodName, servings, totalCalories, totalProtein, totalCarbohydrates, totalFat, totalFiber, totalSodium);
    }

    /**
     * Micronutrient summary for detailed tracking
     */
    public String getMicronutrientSummary() {
        return String.format(Locale.getDefault(),
                "Micronutrients: Iron %.1fmg | Vit D %.1fmcg | B12 %.1fmcg | Folate %.0fmcg | Calcium %.0fmg | Vit C %.1fmg",
                totalIron, totalVitaminD, totalVitaminB12, totalFolate, totalCalcium, totalVitaminC);
    }

    @Override
    public String toString() {
        return "FoodEntry{" +
                "foodName='" + foodName + '\'' +
                ", servings=" + servings +
                ", totalCalories=" + totalCalories +
                ", date='" + date + '\'' +
                '}';
    }
}