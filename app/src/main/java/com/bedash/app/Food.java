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

    // Default constructor for Firebase
    public Food() {}

    public Food(String name, String category, double caloriesPerServing, String servingSize) {
        this.name = name;
        this.category = category;
        this.caloriesPerServing = caloriesPerServing;
        this.servingSize = servingSize;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return name + " (" + caloriesPerServing + " cal per " + servingSize + ")";
    }
}
