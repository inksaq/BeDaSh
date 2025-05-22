package com.bedash.app;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

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

    // Auth reference
    private final FirebaseAuth mAuth;

    // instance
    private static FirestoreManager instance;

    private FirestoreManager() {
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mClientsCollection = mFirestore.collection("clients");
        mNursesCollection = mFirestore.collection("nurses");


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
    public void createNurseProfile(String nurseId, String email, @NonNull Map<String, Object> otherDetails,
                                   final DatabaseCallback<Void> callback) {
        Map<String, Object> nurseData = new HashMap<>(otherDetails); // Copy other details
        nurseData.put("email", email);
        nurseData.put("uid", nurseId); // Storing UID explicitly can be useful
        nurseData.put("createdAt", FieldValue.serverTimestamp());
        nurseData.put("lastLogin", FieldValue.serverTimestamp()); // Initial login

        // Add a status object for tracking online/offline
        Map<String, Object> statusData = new HashMap<>();
        statusData.put("online", false); // Or true if they are immediately considered online
        statusData.put("lastSeen", FieldValue.serverTimestamp());
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
                    .update("lastLogin", FieldValue.serverTimestamp());
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
        clientValues.put("createdAt", FieldValue.serverTimestamp());

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
            statusData.put("lastSeen", FieldValue.serverTimestamp());

            mNursesCollection.document(nurseId).update("status", statusData)
                    .addOnFailureListener(e ->
                            Log.e(TAG, "Error updating online status", e));
        }
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

            // Handle Number types safely
            if (document.contains("age")) {
                Number age = document.get("age", Number.class);
                client.setAge(age != null ? age.intValue() : 0);
            }

            if (document.contains("weight")) {
                Number weight = document.get("weight", Number.class);
                client.setWeight(weight != null ? weight.floatValue() : 0f);
            }

            if (document.contains("height")) {
                Number height = document.get("height", Number.class);
                client.setHeight(height != null ? height.floatValue() : 0f);
            }

            client.setGender(document.getString("gender"));
            client.setActivityLevel(document.getString("activityLevel"));
            client.setEthnicity(document.getString("ethnicity"));
            client.setDietaryPreferences(document.getString("dietaryPreferences"));

            if (document.contains("goalCalories")) {
                Number calories = document.get("goalCalories", Number.class);
                client.setGoalCalories(calories != null ? calories.intValue() : 0);
            }

            if (document.contains("goalProtein")) {
                Number protein = document.get("goalProtein", Number.class);
                client.setGoalProtein(protein != null ? protein.intValue() : 0);
            }

            if (document.contains("goalCarbs")) {
                Number carbs = document.get("goalCarbs", Number.class);
                client.setGoalCarbs(carbs != null ? carbs.intValue() : 0);
            }

            if (document.contains("goalFat")) {
                Number fat = document.get("goalFat", Number.class);
                client.setGoalFat(fat != null ? fat.intValue() : 0);
            }

            return client;
        }
        return null;
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