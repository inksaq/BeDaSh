package com.bedash.app;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodEntry implements Serializable {
    private String id;
    private String clientId;
    private String foodId;
    private String foodName;
    private double servings;
    private double totalCalories;
    private long timestamp;
    private String date; // Format: YYYY-MM-DD for easy querying

    // Default constructor for Firebase
    public FoodEntry() {}

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

    // Getters and Setters
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

    public String getFormattedTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }
}