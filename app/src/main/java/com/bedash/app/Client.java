package com.bedash.app;

import java.io.Serializable;

public class Client implements Serializable {
    private String id;
    private String name;
    private int age;
    private float weight;
    private float height;
    private String gender;
    private String activityLevel;
    private String ethnicity;
    private String dietaryPreferences;
    private int goalCalories;
    private int goalProtein;
    private int goalCarbs;
    private int goalFat;

    // Constructor
    public Client() {
        // Default constructor required for Firebase
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public int getGoalCalories() {
        return goalCalories;
    }

    public void setGoalCalories(int goalCalories) {
        this.goalCalories = goalCalories;
    }

    public int getGoalProtein() {
        return goalProtein;
    }

    public void setGoalProtein(int goalProtein) {
        this.goalProtein = goalProtein;
    }

    public int getGoalCarbs() {
        return goalCarbs;
    }

    public void setGoalCarbs(int goalCarbs) {
        this.goalCarbs = goalCarbs;
    }

    public int getGoalFat() {
        return goalFat;
    }

    public void setGoalFat(int goalFat) {
        this.goalFat = goalFat;
    }

    // Calculate BMI
    public float calculateBMI() {
        if (height > 0) {
            // Convert height from cm to m
            float heightInMeters = height / 100f;
            return weight / (heightInMeters * heightInMeters);
        }
        return 0;
    }

    // Calculate base metabolic rate using Harris-Benedict equation
    public int calculateBMR() {
        if (gender.equalsIgnoreCase("male")) {
            return Math.round(88.362f + (13.397f * weight) + (4.799f * height) - (5.677f * age));
        } else {
            return Math.round(447.593f + (9.247f * weight) + (3.098f * height) - (4.330f * age));
        }
    }

    // Calculate recommended daily calories based on activity level
    public int calculateRecommendedCalories() {
        int bmr = calculateBMR();
        float activityMultiplier = 1.2f; // Default sedentary

        switch (activityLevel.toLowerCase()) {
            case "sedentary":
                activityMultiplier = 1.2f;
                break;
            case "lightly active":
                activityMultiplier = 1.375f;
                break;
            case "moderately active":
                activityMultiplier = 1.55f;
                break;
            case "very active":
                activityMultiplier = 1.725f;
                break;
            case "extra active":
                activityMultiplier = 1.9f;
                break;
        }

        return Math.round(bmr * activityMultiplier);
    }
}
