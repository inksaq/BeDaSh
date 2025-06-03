package com.bedash.app;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for interacting with the OpenFoodFacts API and converting results
 * to Food objects compatible with our Firestore database.
 */
public class OpenFoodFactsAdapter {
    private static final String TAG = "OpenFoodFactsAdapter";

    // Base URL for OpenFoodFacts API
    private static final String BASE_URL = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=";
    private static final String URL_SUFFIX = "&search_simple=1&action=process&json=1&page_size=20";

    // URL for getting a specific product by barcode
    private static final String PRODUCT_URL = "https://world.openfoodfacts.org/api/v0/product/";

    private final Context mContext;
    private final RequestQueue mRequestQueue;

    public OpenFoodFactsAdapter(Context context) {
        this.mContext = context;
        this.mRequestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Search for food items in OpenFoodFacts database
     * @param query The search query
     * @param callback Callback with search results
     */
    public void searchFoods(String query, final SearchCallback callback) {
        String encodedQuery = query.replace(" ", "+");
        String url = BASE_URL + encodedQuery + URL_SUFFIX;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            List<Food> foods = parseSearchResults(response);
                            callback.onSearchSuccess(foods);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing search results", e);
                            callback.onSearchError("Error parsing search results: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error searching OpenFoodFacts", error);
                        callback.onSearchError("Network error: " + error.getMessage());
                    }
                }
        );

        mRequestQueue.add(request);
    }

    /**
     * Get a specific food item by barcode
     * @param barcode The product barcode
     * @param callback Callback with the food item
     */
    public void getFoodByBarcode(String barcode, final FoodCallback callback) {
        String url = PRODUCT_URL + barcode + ".json";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int status = response.getInt("status");
                            if (status == 1) {
                                Food food = parseProductDetails(response.getJSONObject("product"));
                                callback.onFoodSuccess(food);
                            } else {
                                callback.onFoodError("Product not found");
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing product details", e);
                            callback.onFoodError("Error parsing product details: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error getting product from OpenFoodFacts", error);
                        callback.onFoodError("Network error: " + error.getMessage());
                    }
                }
        );

        mRequestQueue.add(request);
    }

    /**
     * Parse search results from OpenFoodFacts API
     * @param response The JSON response from the API
     * @return List of Food objects
     */
    private List<Food> parseSearchResults(JSONObject response) throws JSONException {
        List<Food> foods = new ArrayList<>();

        JSONArray products = response.getJSONArray("products");
        for (int i = 0; i < products.length(); i++) {
            JSONObject product = products.getJSONObject(i);
            Food food = parseProductDetails(product);
            foods.add(food);
        }

        return foods;
    }

    /**
     * Parse product details from OpenFoodFacts API
     * @param product The JSON object containing product details
     * @return Food object
     */
    private Food parseProductDetails(JSONObject product) throws JSONException {
        Food food = new Food();

        // Get product name (use generic name if available, otherwise product name)
        String name = product.optString("generic_name", "");
        if (name.isEmpty()) {
            name = product.optString("product_name", "Unknown Food");
        }
        food.setName(name);

        // Get barcode as ID (we'll replace this with Firestore ID when saving)
        String barcode = product.optString("code", "");
        food.setId(barcode);

        // Get category
        String category = "Other";
        if (product.has("categories_tags") && product.getJSONArray("categories_tags").length() > 0) {
            String categoryTag = product.getJSONArray("categories_tags").getString(0);
            // Clean up category tag (remove "en:" prefix and replace dashes with spaces)
            category = categoryTag.replace("en:", "").replace("-", " ");
            // Capitalize first letter
            category = category.substring(0, 1).toUpperCase() + category.substring(1);
        }
        food.setCategory(category);

        // Get calories per serving
        double calories = 0;
        if (product.has("nutriments")) {
            JSONObject nutriments = product.getJSONObject("nutriments");
            // Try to get energy-kcal per serving, or per 100g if serving not available
            if (nutriments.has("energy-kcal_serving")) {
                calories = nutriments.optDouble("energy-kcal_serving", 0);
            } else if (nutriments.has("energy-kcal")) {
                calories = nutriments.optDouble("energy-kcal", 0);
            }
        }
        food.setCaloriesPerServing(calories);

        // Get serving size
        String servingSize = product.optString("serving_size", "100g");
        if (servingSize.isEmpty()) {
            servingSize = "100g";
        }
        food.setServingSize(servingSize);

        // Set creation timestamp
        food.setCreatedAt(System.currentTimeMillis());

        return food;
    }

    /**
     * Save a food item from OpenFoodFacts to Firestore
     * @param food The food item to save
     * @param callback Callback with the saved food ID
     */
    public void saveToFirestore(Food food, final FirestoreManager.DatabaseCallback<String> callback) {
        FirestoreManager firestoreManager = FirestoreManager.getInstance();

        // Set the creator to the current nurse
        food.setCreatedBy(firestoreManager.getCurrentNurseId());

        // Generate a new ID (the existing ID might be a barcode)
        food.setId(null); // This will cause a new ID to be generated in saveFood

        // Save to Firestore
        firestoreManager.saveFood(food, callback);
    }

    /**
     * Callback interface for search operations
     */
    public interface SearchCallback {
        void onSearchSuccess(List<Food> foods);
        void onSearchError(String errorMessage);
    }

    /**
     * Callback interface for getting a specific food item
     */
    public interface FoodCallback {
        void onFoodSuccess(Food food);
        void onFoodError(String errorMessage);
    }
}