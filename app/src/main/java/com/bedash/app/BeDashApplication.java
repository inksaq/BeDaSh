package com.bedash.app;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BeDashApplication extends Application {
    private static final String TAG = "BeDashApplication";

    // Static instance for singleton access
    private static BeDashApplication instance;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Firebase
        initializeFirebase();
    }

    private void initializeFirebase() {
        try {
            // Initialize Firebase app if not already initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase App initialized");
            }

            // Initialize Auth
            mAuth = FirebaseAuth.getInstance();

            // Initialize Firestore Manager (which handles database setup)
            FirestoreManager.getInstance();

            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
        }
    }

    // Singleton accessor methods
    public static BeDashApplication getInstance() {
        return instance;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirestoreManager getFirestoreManager() {
        return FirestoreManager.getInstance();
    }

    // Track online status methods
    public void setUserOnline() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirestoreManager.getInstance().updateOnlineStatus(user.getUid(), true);
        }
    }

    public void setUserOffline() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirestoreManager.getInstance().updateOnlineStatus(user.getUid(), false);
        }
    }
}