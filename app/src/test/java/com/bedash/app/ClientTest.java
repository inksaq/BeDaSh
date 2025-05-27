package com.bedash.app;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    private Client client;

    @Before
    public void setUp() {
        client = new Client();
        client.setAge(30);
        client.setWeight(80.0f);
        client.setHeight(180.0f);
        client.setGender("Male");
        client.setActivityLevel("Moderately active");
    }

    @Test
    public void calculateBMI_returnsCorrectValue() {
        // BMI = weight / (height in meters)²
        // 80 / (1.8)² = 80 / 3.24 = 24.69
        float expectedBmi = 24.69f;
        float actualBmi = client.calculateBMI();

        // Allow for small floating-point differences
        assertEquals(expectedBmi, actualBmi, 0.1f);
    }

    @Test
    public void calculateBMR_forMale_returnsCorrectValue() {
        // Using Harris-Benedict equation for males:
        // BMR = 88.362 + (13.397 × weight in kg) + (4.799 × height in cm) - (5.677 × age in years)
        int expectedBmr = 1882; // Calculated based on the formula with the given values
        int actualBmr = client.calculateBMR();

        // Allow for small rounding differences
        assertEquals(expectedBmr, actualBmr, 5);
    }

    @Test
    public void calculateBMR_forFemale_returnsCorrectValue() {
        // Using Harris-Benedict equation for females:
        // BMR = 447.593 + (9.247 × weight in kg) + (3.098 × height in cm) - (4.330 × age in years)
        client.setGender("Female");

        int expectedBmr = 1627; // Calculated based on the formula with the given values
        int actualBmr = client.calculateBMR();

        // Allow for small rounding differences
        assertEquals(expectedBmr, actualBmr, 5);
    }

    @Test
    public void calculateRecommendedCalories_returnsCorrectValue() {
        // For "Moderately active", the multiplier is 1.55
        // Recommended calories = BMR * activity multiplier
        // BMR for our test male = ~1882
        // Recommended calories = 1882 * 1.55 = ~2917

        int expectedCalories = 2917;
        int actualCalories = client.calculateRecommendedCalories();

        // Allow for small rounding differences
        assertEquals(expectedCalories, actualCalories, 5);
    }

    @Test
    public void calculateRecommendedCalories_forDifferentActivity_returnsCorrectValue() {
        // Change activity level to "Very active" (multiplier 1.725)
        client.setActivityLevel("Very active");

        // Recalculate expected calories
        // BMR for our test male = ~1882
        // Recommended calories = 1882 * 1.725 = ~3246

        int expectedCalories = 3246;
        int actualCalories = client.calculateRecommendedCalories();

        // Allow for small rounding differences
        assertEquals(expectedCalories, actualCalories, 5);
    }
}
