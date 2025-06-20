package com.bedash.app;

import java.io.Serializable;

public class Food implements Serializable {
    private String id;
    private String name;
    private String category;
    private double caloriesPerServing;
    private String servingSize;
    private String createdBy; // nurse ID who created this food item
    private long createdAt;

    // Macronutrients (grams per serving)
    private double protein = 0.0;
    private double totalCarbohydrates = 0.0;
    private double dietaryFiber = 0.0;
    private double totalSugars = 0.0;
    private double totalFat = 0.0;
    private double saturatedFat = 0.0;
    private double transFat = 0.0;

    // Essential Micronutrients
    // Tier 1: Critical micronutrients (milligrams per serving)
    private double iron = 0.0;           // mg
    private double vitaminD = 0.0;       // mcg (converted from IU if needed: mcg = IU / 40)
    private double vitaminB12 = 0.0;     // mcg
    private double folate = 0.0;         // mcg DFE (Dietary Folate Equivalents)
    private double magnesium = 0.0;      // mg

    // Tier 2: Important secondary micronutrients
    private double calcium = 0.0;        // mg
    private double zinc = 0.0;           // mg
    private double potassium = 0.0;      // mg
    private double sodium = 0.0;         // mg
    private double vitaminC = 0.0;       // mg

    // Default constructor for Firebase
    public Food() {}

    public Food(String name, String category, double caloriesPerServing, String servingSize) {
        this.name = name;
        this.category = category;
        this.caloriesPerServing = caloriesPerServing;
        this.servingSize = servingSize;
        this.createdAt = System.currentTimeMillis();
    }

    // Basic getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getCaloriesPerServing() {
        return caloriesPerServing;
    }

    public void setCaloriesPerServing(double caloriesPerServing) {
        this.caloriesPerServing = caloriesPerServing;
    }

    public String getServingSize() {
        return servingSize;
    }

    public void setServingSize(String servingSize) {
        this.servingSize = servingSize;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Macronutrient getters and setters
    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getTotalCarbohydrates() {
        return totalCarbohydrates;
    }

    public void setTotalCarbohydrates(double totalCarbohydrates) {
        this.totalCarbohydrates = totalCarbohydrates;
    }

    public double getDietaryFiber() {
        return dietaryFiber;
    }

    public void setDietaryFiber(double dietaryFiber) {
        this.dietaryFiber = dietaryFiber;
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

    public double getSaturatedFat() {
        return saturatedFat;
    }

    public void setSaturatedFat(double saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public double getTransFat() {
        return transFat;
    }

    public void setTransFat(double transFat) {
        this.transFat = transFat;
    }

    // Tier 1 Micronutrient getters and setters
    public double getIron() {
        return iron;
    }

    public void setIron(double iron) {
        this.iron = iron;
    }

    public double getVitaminD() {
        return vitaminD;
    }

    public void setVitaminD(double vitaminD) {
        this.vitaminD = vitaminD;
    }

    public double getVitaminB12() {
        return vitaminB12;
    }

    public void setVitaminB12(double vitaminB12) {
        this.vitaminB12 = vitaminB12;
    }

    public double getFolate() {
        return folate;
    }

    public void setFolate(double folate) {
        this.folate = folate;
    }

    public double getMagnesium() {
        return magnesium;
    }

    public void setMagnesium(double magnesium) {
        this.magnesium = magnesium;
    }

    // Tier 2 Micronutrient getters and setters
    public double getCalcium() {
        return calcium;
    }

    public void setCalcium(double calcium) {
        this.calcium = calcium;
    }

    public double getZinc() {
        return zinc;
    }

    public void setZinc(double zinc) {
        this.zinc = zinc;
    }

    public double getPotassium() {
        return potassium;
    }

    public void setPotassium(double potassium) {
        this.potassium = potassium;
    }

    public double getSodium() {
        return sodium;
    }

    public void setSodium(double sodium) {
        this.sodium = sodium;
    }

    public double getVitaminC() {
        return vitaminC;
    }

    public void setVitaminC(double vitaminC) {
        this.vitaminC = vitaminC;
    }

    // Helper methods for nutrition calculations
    public double getNetCarbs() {
        return Math.max(0, totalCarbohydrates - dietaryFiber);
    }

    public double getCaloriesFromMacros() {
        return (protein * 4) + (totalCarbohydrates * 4) + (totalFat * 9);
    }

    public double getProteinPercentage() {
        if (caloriesPerServing <= 0) return 0;
        return (protein * 4 / caloriesPerServing) * 100;
    }

    public double getCarbsPercentage() {
        if (caloriesPerServing <= 0) return 0;
        return (totalCarbohydrates * 4 / caloriesPerServing) * 100;
    }

    public double getFatPercentage() {
        if (caloriesPerServing <= 0) return 0;
        return (totalFat * 9 / caloriesPerServing) * 100;
    }

    // Clinical assessment helpers
    public boolean isHighSodium() {
        return sodium > 480; // >20% DV per serving considered high
    }

    public boolean isHighFiber() {
        return dietaryFiber >= 5; // ≥5g per serving considered high fiber
    }

    public boolean isLowSaturatedFat() {
        return saturatedFat < 1; // <1g per serving considered low
    }

    @Override
    public String toString() {
        return name + " (" + caloriesPerServing + " cal per " + servingSize + ")";
    }

    // Detailed nutrition summary for healthcare professionals
    public String getNutritionSummary() {
        return String.format("Nutrition per %s:\nCalories: %.0f\nProtein: %.1fg\nCarbs: %.1fg (Fiber: %.1fg)\nFat: %.1fg (Sat: %.1fg)\nSodium: %.0fmg",
                servingSize, caloriesPerServing, protein, totalCarbohydrates, dietaryFiber, totalFat, saturatedFat, sodium);
    }
}