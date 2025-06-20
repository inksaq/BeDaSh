package com.bedash.app;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized manager for all Firestore database operations
 */
public class FirestoreManager {
    private static final String TAG = "FirestoreManager";

    // Firestore references
    private final FirebaseFirestore mFirestore;
    private final CollectionReference mClientsCollection;
    private final CollectionReference mNursesCollection;
    private final CollectionReference mFoodsCollection;
    private final CollectionReference mFoodEntriesCollection;

    // Auth reference
    private final FirebaseAuth mAuth;

    // instance
    private static FirestoreManager instance;

    private FirestoreManager() {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mClientsCollection = mFirestore.collection("clients");
        mNursesCollection = mFirestore.collection("nurses");
        mFoodsCollection = mFirestore.collection("foods");
        mFoodEntriesCollection = mFirestore.collection("food_entries");

        // Configure settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        mFirestore.setFirestoreSettings(settings);
    }

    // Singleton accessor (Manager Getter)
    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    /**
     * Get the current logged in nurse ID
     * @return nurse ID or null if not logged in
     */
    public String getCurrentNurseId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Create a new nurse profile in the database after registration
     * @param nurseId The nurse's user ID
     * @param email The nurse's email
     * @param callback Optional callback when operation completes
     */
    public void createNurseProfile(String nurseId, String email, Map<String, Object> otherDetails,
                                   final DatabaseCallback<Void> callback) {
        Map<String, Object> nurseData = new HashMap<>(otherDetails); // Copy other details
        nurseData.put("email", email);
        nurseData.put("uid", nurseId); // Storing UID explicitly can be useful
        nurseData.put("createdAt", System.currentTimeMillis());
        nurseData.put("lastLogin", System.currentTimeMillis()); // Initial login

        // Add a status object for tracking online/offline
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("online", false); // Or true if they are immediately considered online
        statusData.put("lastSeen", System.currentTimeMillis());
        nurseData.put("status", statusData);

        // The document ID will be the nurseId (Firebase UID)
        DocumentReference nurseRef = mNursesCollection.document(nurseId);

        nurseRef.set(nurseData) // Using set() to create or overwrite if it somehow existed
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Nurse profile created/updated successfully for ID: " + nurseId);
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating/updating nurse profile for ID: " + nurseId + " Error: " + e.getMessage());
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * This method is kept for now but might be deprecated or removed if
     * the responsibility of Auth creation is fully moved to the Activity/ViewModel.
     * It handles registration with rollback capability.
     * @param email User email
     * @param password User password
     * @param authCallback Callback with authentication result (returns nurseId/UID)
     */
    public void registerNurseWithFirebase(String email, String password,
                                          final DatabaseCallback<String> authCallback) {
        // This FirebaseAuth instance is local to the method, or you could use mAuth if preferred.
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> { // Changed to addOnCompleteListener for better error handling
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            String nurseId = user.getUid();
                            Map<String, Object> initialDetails = new HashMap<>(); // Add any other default details here if needed

                            // Create nurse profile in Firestore
                            createNurseProfile(nurseId, email, initialDetails, new DatabaseCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    if (authCallback != null) {
                                        authCallback.onSuccess(nurseId);
                                    }
                                }

                                @Override
                                public void onError(Exception error) {
                                    Log.e(TAG, "Firestore profile creation failed for " + nurseId + ". Attempting to delete Auth user.", error);
                                    user.delete().addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            Log.d(TAG, "Auth user " + nurseId + " deleted due to Firestore profile creation failure.");
                                        } else {
                                            Log.e(TAG, "Failed to delete Auth user " + nurseId + " after Firestore failure.", deleteTask.getException());
                                        }
                                        // Propagate the original Firestore error
                                        if (authCallback != null) {
                                            authCallback.onError(new Exception("Failed to create profile in Firestore: " + error.getMessage(), error));
                                        }
                                    });
                                }
                            });
                        } else {
                            // Should not happen if task is successful, but good to handle
                            if (authCallback != null) {
                                authCallback.onError(new Exception("Firebase user was null after successful authentication."));
                            }
                        }
                    } else {
                        // Auth creation failed
                        if (authCallback != null) {
                            authCallback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Update the nurse's last login timestamp
     * @param nurseId The nurse's user ID
     */
    public void updateNurseLastLogin(String nurseId) {
        if (nurseId != null) {
            mNursesCollection.document(nurseId)
                    .update("lastLogin", System.currentTimeMillis());
        }
    }

    /**
     * Get a nurse's profile data
     * @param nurseId The nurse's user ID
     * @param callback Callback with the nurse data
     */
    public void getNurseProfile(String nurseId, final DatabaseCallback<DocumentSnapshot> callback) {
        mNursesCollection.document(nurseId).get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Update nurse profile information
     * @param nurseId The nurse's user ID
     * @param data Map of fields to update
     * @param callback Optional callback when operation completes
     */
    public void updateNurseProfile(String nurseId, Map<String, Object> data,
                                   final DatabaseCallback<Void> callback) {
        mNursesCollection.document(nurseId).update(data)
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Save a client to the database
     * @param client The client object to save
     * @param callback Callback for operation result
     */
    public void saveClient(Client client, final DatabaseCallback<String> callback) {
        String nurseId = getCurrentNurseId();
        if (nurseId == null) {
            if (callback != null) {
                callback.onError(new Exception("Not logged in"));
            }
            return;
        }

        // Generate a new client ID if needed
        if (client.getId() == null || client.getId().isEmpty()) {
            client.setId(mClientsCollection.document().getId());
        }

        // Create client data map
        Map<String, Object> clientValues = new HashMap<>();
        clientValues.put("id", client.getId());
        clientValues.put("name", client.getName());
        clientValues.put("age", client.getAge());
        clientValues.put("weight", client.getWeight());
        clientValues.put("height", client.getHeight());
        clientValues.put("gender", client.getGender());
        clientValues.put("activityLevel", client.getActivityLevel());

        if (client.getEthnicity() != null) {
            clientValues.put("ethnicity", client.getEthnicity());
        }

        if (client.getDietaryPreferences() != null) {
            clientValues.put("dietaryPreferences", client.getDietaryPreferences());
        }

        clientValues.put("goalCalories", client.getGoalCalories());
        clientValues.put("goalProtein", client.getGoalProtein());
        clientValues.put("goalCarbs", client.getGoalCarbs());
        clientValues.put("goalFat", client.getGoalFat());
        clientValues.put("nurseId", nurseId);
        clientValues.put("createdAt", System.currentTimeMillis()); // Use actual timestamp

        // Create or update client document
        mClientsCollection.document(client.getId()).set(clientValues)
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(client.getId());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Delete a client
     * @param clientId The client ID to delete
     * @param callback Optional callback when operation completes
     */
    public void deleteClient(String clientId, final DatabaseCallback<Void> callback) {
        mClientsCollection.document(clientId).delete()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get a client by ID
     * @param clientId The client ID
     * @param callback Callback with client data
     */
    public void getClient(String clientId, final DatabaseCallback<DocumentSnapshot> callback) {
        mClientsCollection.document(clientId).get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get all clients for the current nurse
     * @param callback Callback with client list snapshot
     */
    public void getNurseClients(final DatabaseCallback<QuerySnapshot> callback) {
        String nurseId = getCurrentNurseId();
        if (nurseId == null) {
            if (callback != null) {
                callback.onError(new Exception("Not logged in"));
            }
            return;
        }

        mClientsCollection.whereEqualTo("nurseId", nurseId).get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Set up a real-time listener for a nurse's clients
     * @param nurseId The nurse's ID
     * @param listener The snapshot listener to receive updates
     * @return The query being listened to (can be used to remove listener)
     */
    public Query setupNurseClientsListener(String nurseId, com.google.firebase.firestore.EventListener<QuerySnapshot> listener) {
        Query query = mClientsCollection.whereEqualTo("nurseId", nurseId);
        query.addSnapshotListener(listener);
        return query;
    }

    /**
     * Update the online status of the nurse
     * Note: Firestore doesn't have onDisconnect functionality like Realtime Database
     * @param nurseId The nurse ID
     * @param online Whether the nurse is online
     */
    public void updateOnlineStatus(String nurseId, boolean online) {
        if (nurseId != null) {
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("online", online);
            statusData.put("lastSeen", System.currentTimeMillis());

            // First check if document exists, if not create it
            DocumentReference nurseRef = mNursesCollection.document(nurseId);
            nurseRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Document exists, update status
                        nurseRef.update("status", statusData)
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error updating online status", e));
                    } else {
                        // Document doesn't exist, create it with minimal data
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            Map<String, Object> nurseData = new HashMap<>();
                            nurseData.put("email", currentUser.getEmail());
                            nurseData.put("uid", nurseId);
                            nurseData.put("createdAt", System.currentTimeMillis());
                            nurseData.put("status", statusData);

                            nurseRef.set(nurseData)
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error creating nurse document", e));
                        }
                    }
                } else {
                    Log.e(TAG, "Error checking nurse document existence", task.getException());
                }
            });
        }
    }

    // ========== FOOD MANAGEMENT METHODS ==========

    /**
     * Save a food item to the database with complete nutrition information
     * @param food The food object to save
     * @param callback Callback for operation result
     */
    public void saveFood(Food food, final DatabaseCallback<String> callback) {
        String nurseId = getCurrentNurseId();
        if (nurseId == null) {
            if (callback != null) {
                callback.onError(new Exception("Not logged in"));
            }
            return;
        }

        // Generate a new food ID if needed
        if (food.getId() == null || food.getId().isEmpty()) {
            food.setId(mFoodsCollection.document().getId());
        }

        // Set the creator
        food.setCreatedBy(nurseId);
        food.setCreatedAt(System.currentTimeMillis());

        // Create comprehensive food data map including all nutrition information
        Map<String, Object> foodValues = new HashMap<>();

        // Basic information
        foodValues.put("id", food.getId());
        foodValues.put("name", food.getName());
        foodValues.put("category", food.getCategory());
        foodValues.put("caloriesPerServing", food.getCaloriesPerServing());
        foodValues.put("servingSize", food.getServingSize());
        foodValues.put("createdBy", food.getCreatedBy());
        foodValues.put("createdAt", food.getCreatedAt());

        // Macronutrients
        foodValues.put("protein", food.getProtein());
        foodValues.put("totalCarbohydrates", food.getTotalCarbohydrates());
        foodValues.put("dietaryFiber", food.getDietaryFiber());
        foodValues.put("totalSugars", food.getTotalSugars());
        foodValues.put("totalFat", food.getTotalFat());
        foodValues.put("saturatedFat", food.getSaturatedFat());
        foodValues.put("transFat", food.getTransFat());

        // Critical micronutrients
        foodValues.put("iron", food.getIron());
        foodValues.put("vitaminD", food.getVitaminD());
        foodValues.put("vitaminB12", food.getVitaminB12());
        foodValues.put("folate", food.getFolate());
        foodValues.put("magnesium", food.getMagnesium());

        // Important micronutrients
        foodValues.put("calcium", food.getCalcium());
        foodValues.put("zinc", food.getZinc());
        foodValues.put("potassium", food.getPotassium());
        foodValues.put("sodium", food.getSodium());
        foodValues.put("vitaminC", food.getVitaminC());

        // Create or update food document
        mFoodsCollection.document(food.getId()).set(foodValues)
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(food.getId());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get all food items
     * @param callback Callback with food list snapshot
     */
    public void getAllFoods(final DatabaseCallback<QuerySnapshot> callback) {
        mFoodsCollection.orderBy("name").get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Save a food entry to the database with complete nutrition information
     * @param foodEntry The food entry object to save
     * @param callback Callback for operation result
     */
    public void saveFoodEntry(FoodEntry foodEntry, final DatabaseCallback<String> callback) {
        // Generate a new entry ID if needed
        if (foodEntry.getId() == null || foodEntry.getId().isEmpty()) {
            foodEntry.setId(mFoodEntriesCollection.document().getId());
        }

        // Create comprehensive food entry data map including all nutrition information
        Map<String, Object> entryValues = new HashMap<>();

        // Basic information
        entryValues.put("id", foodEntry.getId());
        entryValues.put("clientId", foodEntry.getClientId());
        entryValues.put("foodId", foodEntry.getFoodId());
        entryValues.put("foodName", foodEntry.getFoodName());
        entryValues.put("servings", foodEntry.getServings());
        entryValues.put("totalCalories", foodEntry.getTotalCalories());
        entryValues.put("timestamp", foodEntry.getTimestamp());
        entryValues.put("date", foodEntry.getDate());

        // Total nutrition values (calculated from servings)
        entryValues.put("totalProtein", foodEntry.getTotalProtein());
        entryValues.put("totalCarbohydrates", foodEntry.getTotalCarbohydrates());
        entryValues.put("totalFiber", foodEntry.getTotalFiber());
        entryValues.put("totalSugars", foodEntry.getTotalSugars());
        entryValues.put("totalFat", foodEntry.getTotalFat());
        entryValues.put("totalSaturatedFat", foodEntry.getTotalSaturatedFat());
        entryValues.put("totalSodium", foodEntry.getTotalSodium());

        // Total micronutrients
        entryValues.put("totalIron", foodEntry.getTotalIron());
        entryValues.put("totalVitaminD", foodEntry.getTotalVitaminD());
        entryValues.put("totalVitaminB12", foodEntry.getTotalVitaminB12());
        entryValues.put("totalFolate", foodEntry.getTotalFolate());
        entryValues.put("totalCalcium", foodEntry.getTotalCalcium());
        entryValues.put("totalVitaminC", foodEntry.getTotalVitaminC());

        // Create or update food entry document
        mFoodEntriesCollection.document(foodEntry.getId()).set(entryValues)
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(foodEntry.getId());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get food entries for a specific client and date
     * @param clientId The client ID
     * @param date The date in YYYY-MM-DD format
     * @param callback Callback with food entries snapshot
     */
    public void getFoodEntriesForDate(String clientId, String date, final DatabaseCallback<QuerySnapshot> callback) {
        mFoodEntriesCollection
                .whereEqualTo("clientId", clientId)
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get food entries for a specific client within a date range
     * @param clientId The client ID
     * @param startDate The start date in YYYY-MM-DD format
     * @param endDate The end date in YYYY-MM-DD format
     * @param callback Callback with food entries snapshot
     */
    public void getFoodEntriesForDateRange(String clientId, String startDate, String endDate,
                                           final DatabaseCallback<QuerySnapshot> callback) {
        mFoodEntriesCollection
                .whereEqualTo("clientId", clientId)
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Delete a food entry
     * @param entryId The food entry ID to delete
     * @param callback Optional callback when operation completes
     */
    public void deleteFoodEntry(String entryId, final DatabaseCallback<Void> callback) {
        mFoodEntriesCollection.document(entryId).delete()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Convert Client object from a Firestore document
     * @param document The document to convert
     * @return A Client object, or null if document doesn't exist
     */
    public Client documentToClient(DocumentSnapshot document) {
        if (document.exists()) {
            Client client = new Client();
            client.setId(document.getId());
            client.setName(document.getString("name"));

            // Handle Number types safely using Long/Double and convert to needed types
            if (document.contains("age")) {
                Long age = document.getLong("age");
                client.setAge(age != null ? age.intValue() : 0);
            }

            if (document.contains("weight")) {
                Double weight = document.getDouble("weight");
                client.setWeight(weight != null ? weight.floatValue() : 0f);
            }

            if (document.contains("height")) {
                Double height = document.getDouble("height");
                client.setHeight(height != null ? height.floatValue() : 0f);
            }

            client.setGender(document.getString("gender"));
            client.setActivityLevel(document.getString("activityLevel"));
            client.setEthnicity(document.getString("ethnicity"));
            client.setDietaryPreferences(document.getString("dietaryPreferences"));

            if (document.contains("goalCalories")) {
                Long calories = document.getLong("goalCalories");
                client.setGoalCalories(calories != null ? calories.intValue() : 0);
            }

            if (document.contains("goalProtein")) {
                Long protein = document.getLong("goalProtein");
                client.setGoalProtein(protein != null ? protein.intValue() : 0);
            }

            if (document.contains("goalCarbs")) {
                Long carbs = document.getLong("goalCarbs");
                client.setGoalCarbs(carbs != null ? carbs.intValue() : 0);
            }

            if (document.contains("goalFat")) {
                Long fat = document.getLong("goalFat");
                client.setGoalFat(fat != null ? fat.intValue() : 0);
            }

            return client;
        }
        return null;
    }

    /**
     * Convert Food object from a Firestore document with complete nutrition information
     * @param document The document to convert
     * @return A Food object, or null if document doesn't exist
     */
    public Food documentToFood(DocumentSnapshot document) {
        if (document.exists()) {
            Food food = new Food();
            food.setId(document.getId());
            food.setName(document.getString("name"));
            food.setCategory(document.getString("category"));
            food.setServingSize(document.getString("servingSize"));
            food.setCreatedBy(document.getString("createdBy"));

            // Basic nutrition
            if (document.contains("caloriesPerServing")) {
                Double calories = document.getDouble("caloriesPerServing");
                food.setCaloriesPerServing(calories != null ? calories : 0.0);
            }

            // Macronutrients
            food.setProtein(getDoubleFromDocument(document, "protein"));
            food.setTotalCarbohydrates(getDoubleFromDocument(document, "totalCarbohydrates"));
            food.setDietaryFiber(getDoubleFromDocument(document, "dietaryFiber"));
            food.setTotalSugars(getDoubleFromDocument(document, "totalSugars"));
            food.setTotalFat(getDoubleFromDocument(document, "totalFat"));
            food.setSaturatedFat(getDoubleFromDocument(document, "saturatedFat"));
            food.setTransFat(getDoubleFromDocument(document, "transFat"));

            // Critical micronutrients
            food.setIron(getDoubleFromDocument(document, "iron"));
            food.setVitaminD(getDoubleFromDocument(document, "vitaminD"));
            food.setVitaminB12(getDoubleFromDocument(document, "vitaminB12"));
            food.setFolate(getDoubleFromDocument(document, "folate"));
            food.setMagnesium(getDoubleFromDocument(document, "magnesium"));

            // Important micronutrients
            food.setCalcium(getDoubleFromDocument(document, "calcium"));
            food.setZinc(getDoubleFromDocument(document, "zinc"));
            food.setPotassium(getDoubleFromDocument(document, "potassium"));
            food.setSodium(getDoubleFromDocument(document, "sodium"));
            food.setVitaminC(getDoubleFromDocument(document, "vitaminC"));

            // Timestamp handling
            if (document.contains("createdAt")) {
                Object timestampObj = document.get("createdAt");
                if (timestampObj instanceof Long) {
                    food.setCreatedAt((Long) timestampObj);
                } else if (timestampObj instanceof com.google.firebase.Timestamp) {
                    food.setCreatedAt(((com.google.firebase.Timestamp) timestampObj).toDate().getTime());
                } else {
                    food.setCreatedAt(System.currentTimeMillis());
                }
            } else {
                food.setCreatedAt(System.currentTimeMillis());
            }

            return food;
        }
        return null;
    }

    /**
     * Convert FoodEntry object from a Firestore document with complete nutrition information
     * @param document The document to convert
     * @return A FoodEntry object, or null if document doesn't exist
     */
    public FoodEntry documentToFoodEntry(DocumentSnapshot document) {
        if (document.exists()) {
            FoodEntry entry = new FoodEntry();
            entry.setId(document.getId());
            entry.setClientId(document.getString("clientId"));
            entry.setFoodId(document.getString("foodId"));
            entry.setFoodName(document.getString("foodName"));
            entry.setDate(document.getString("date"));

            // Basic values
            entry.setServings(getDoubleFromDocument(document, "servings"));
            entry.setTotalCalories(getDoubleFromDocument(document, "totalCalories"));

            // Total macronutrients
            entry.setTotalProtein(getDoubleFromDocument(document, "totalProtein"));
            entry.setTotalCarbohydrates(getDoubleFromDocument(document, "totalCarbohydrates"));
            entry.setTotalFiber(getDoubleFromDocument(document, "totalFiber"));
            entry.setTotalSugars(getDoubleFromDocument(document, "totalSugars"));
            entry.setTotalFat(getDoubleFromDocument(document, "totalFat"));
            entry.setTotalSaturatedFat(getDoubleFromDocument(document, "totalSaturatedFat"));
            entry.setTotalSodium(getDoubleFromDocument(document, "totalSodium"));

            // Total micronutrients
            entry.setTotalIron(getDoubleFromDocument(document, "totalIron"));
            entry.setTotalVitaminD(getDoubleFromDocument(document, "totalVitaminD"));
            entry.setTotalVitaminB12(getDoubleFromDocument(document, "totalVitaminB12"));
            entry.setTotalFolate(getDoubleFromDocument(document, "totalFolate"));
            entry.setTotalCalcium(getDoubleFromDocument(document, "totalCalcium"));
            entry.setTotalVitaminC(getDoubleFromDocument(document, "totalVitaminC"));

            // Timestamp handling
            if (document.contains("timestamp")) {
                Object timestampObj = document.get("timestamp");
                if (timestampObj instanceof Long) {
                    entry.setTimestamp((Long) timestampObj);
                } else if (timestampObj instanceof com.google.firebase.Timestamp) {
                    entry.setTimestamp(((com.google.firebase.Timestamp) timestampObj).toDate().getTime());
                } else {
                    entry.setTimestamp(System.currentTimeMillis());
                }
            } else {
                entry.setTimestamp(System.currentTimeMillis());
            }

            return entry;
        }
        return null;
    }

    /**
     * Helper method to safely get double values from Firestore documents
     * @param document The Firestore document
     * @param field The field name
     * @return The double value, or 0.0 if not found or invalid
     */
    private double getDoubleFromDocument(DocumentSnapshot document, String field) {
        if (document.contains(field)) {
            Double value = document.getDouble(field);
            return value != null ? value : 0.0;
        }
        return 0.0;
    }

    /**
     * Search for food items by name
     * @param searchQuery The search query (partial name matching)
     * @param callback Callback with search results
     */
    public void searchFoods(String searchQuery, final DatabaseCallback<QuerySnapshot> callback) {
        // Firestore doesn't support full-text search, so we'll use a simple approach
        // For production apps, consider using Algolia or Elasticsearch for better search
        String searchLower = searchQuery.toLowerCase();

        mFoodsCollection
                .orderBy("name")
                .startAt(searchLower)
                .endAt(searchLower + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get foods by category
     * @param category The food category
     * @param callback Callback with filtered results
     */
    public void getFoodsByCategory(String category, final DatabaseCallback<QuerySnapshot> callback) {
        mFoodsCollection
                .whereEqualTo("category", category)
                .orderBy("name")
                .get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Delete a food item
     * @param foodId The food ID to delete
     * @param callback Optional callback when operation completes
     */
    public void deleteFood(String foodId, final DatabaseCallback<Void> callback) {
        mFoodsCollection.document(foodId).delete()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get a food item by ID
     * @param foodId The food ID
     * @param callback Callback with food data
     */
    public void getFood(String foodId, final DatabaseCallback<DocumentSnapshot> callback) {
        mFoodsCollection.document(foodId).get()
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(task.getResult());
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    /**
     * Get daily nutrition summary for a client on a specific date
     * @param clientId The client ID
     * @param date The date in YYYY-MM-DD format
     * @param callback Callback with aggregated nutrition data
     */
    public void getDailyNutritionSummary(String clientId, String date,
                                         final DatabaseCallback<Map<String, Double>> callback) {
        getFoodEntriesForDate(clientId, date, new DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                Map<String, Double> nutritionSummary = new HashMap<>();

                // Initialize totals
                nutritionSummary.put("totalCalories", 0.0);
                nutritionSummary.put("totalProtein", 0.0);
                nutritionSummary.put("totalCarbohydrates", 0.0);
                nutritionSummary.put("totalFiber", 0.0);
                nutritionSummary.put("totalSugars", 0.0);
                nutritionSummary.put("totalFat", 0.0);
                nutritionSummary.put("totalSaturatedFat", 0.0);
                nutritionSummary.put("totalSodium", 0.0);
                nutritionSummary.put("totalIron", 0.0);
                nutritionSummary.put("totalVitaminD", 0.0);
                nutritionSummary.put("totalVitaminB12", 0.0);
                nutritionSummary.put("totalFolate", 0.0);
                nutritionSummary.put("totalCalcium", 0.0);
                nutritionSummary.put("totalVitaminC", 0.0);

                // Sum up all entries
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    FoodEntry entry = documentToFoodEntry(document);
                    if (entry != null) {
                        nutritionSummary.put("totalCalories",
                                nutritionSummary.get("totalCalories") + entry.getTotalCalories());
                        nutritionSummary.put("totalProtein",
                                nutritionSummary.get("totalProtein") + entry.getTotalProtein());
                        nutritionSummary.put("totalCarbohydrates",
                                nutritionSummary.get("totalCarbohydrates") + entry.getTotalCarbohydrates());
                        nutritionSummary.put("totalFiber",
                                nutritionSummary.get("totalFiber") + entry.getTotalFiber());
                        nutritionSummary.put("totalSugars",
                                nutritionSummary.get("totalSugars") + entry.getTotalSugars());
                        nutritionSummary.put("totalFat",
                                nutritionSummary.get("totalFat") + entry.getTotalFat());
                        nutritionSummary.put("totalSaturatedFat",
                                nutritionSummary.get("totalSaturatedFat") + entry.getTotalSaturatedFat());
                        nutritionSummary.put("totalSodium",
                                nutritionSummary.get("totalSodium") + entry.getTotalSodium());
                        nutritionSummary.put("totalIron",
                                nutritionSummary.get("totalIron") + entry.getTotalIron());
                        nutritionSummary.put("totalVitaminD",
                                nutritionSummary.get("totalVitaminD") + entry.getTotalVitaminD());
                        nutritionSummary.put("totalVitaminB12",
                                nutritionSummary.get("totalVitaminB12") + entry.getTotalVitaminB12());
                        nutritionSummary.put("totalFolate",
                                nutritionSummary.get("totalFolate") + entry.getTotalFolate());
                        nutritionSummary.put("totalCalcium",
                                nutritionSummary.get("totalCalcium") + entry.getTotalCalcium());
                        nutritionSummary.put("totalVitaminC",
                                nutritionSummary.get("totalVitaminC") + entry.getTotalVitaminC());
                    }
                }

                if (callback != null) {
                    callback.onSuccess(nutritionSummary);
                }
            }

            @Override
            public void onError(Exception error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    /**
     * Update an existing food entry
     * @param foodEntry The updated food entry
     * @param callback Optional callback when operation completes
     */
    public void updateFoodEntry(FoodEntry foodEntry, final DatabaseCallback<Void> callback) {
        if (foodEntry.getId() == null || foodEntry.getId().isEmpty()) {
            if (callback != null) {
                callback.onError(new Exception("Food entry ID is required for updates"));
            }
            return;
        }

        // Create update map with all nutrition information
        Map<String, Object> updates = new HashMap<>();
        updates.put("servings", foodEntry.getServings());
        updates.put("totalCalories", foodEntry.getTotalCalories());
        updates.put("totalProtein", foodEntry.getTotalProtein());
        updates.put("totalCarbohydrates", foodEntry.getTotalCarbohydrates());
        updates.put("totalFiber", foodEntry.getTotalFiber());
        updates.put("totalSugars", foodEntry.getTotalSugars());
        updates.put("totalFat", foodEntry.getTotalFat());
        updates.put("totalSaturatedFat", foodEntry.getTotalSaturatedFat());
        updates.put("totalSodium", foodEntry.getTotalSodium());
        updates.put("totalIron", foodEntry.getTotalIron());
        updates.put("totalVitaminD", foodEntry.getTotalVitaminD());
        updates.put("totalVitaminB12", foodEntry.getTotalVitaminB12());
        updates.put("totalFolate", foodEntry.getTotalFolate());
        updates.put("totalCalcium", foodEntry.getTotalCalcium());
        updates.put("totalVitaminC", foodEntry.getTotalVitaminC());

        mFoodEntriesCollection.document(foodEntry.getId()).update(updates)
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    // Utility method to safely update a document field
    public void updateField(String collection, String documentId, String field, Object value,
                            final DatabaseCallback<Void> callback) {
        mFirestore.collection(collection).document(documentId)
                .update(field, value)
                .addOnCompleteListener(task -> {
                    if (callback != null) {
                        if (task.isSuccessful()) {
                            callback.onSuccess(null);
                        } else {
                            callback.onError(task.getException());
                        }
                    }
                });
    }

    // Callback interface for database operations
    public interface DatabaseCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}