package com.bedash.app;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BeDashApplication extends Application {
    private static final String TAG = "BeDashApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            // No Firebase app has been initialized yet
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase App initialized");
        }

        // Initialize and configure Firebase Database
        try {
            // Get database instance
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // Enable offline persistence
            database.setPersistenceEnabled(true);

            // Set keep-sync on the main nodes
            database.getReference("clients").keepSynced(true);
            database.getReference("trainers").keepSynced(true);

            // Check connection status
            DatabaseReference connectedRef = database.getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class) != null &&
                            snapshot.getValue(Boolean.class);
                    if (connected) {
                        Log.d(TAG, "Connected to Firebase Database");
                    } else {
                        Log.w(TAG, "Disconnected from Firebase Database");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Firebase connection listener was cancelled", error.toException());
                }
            });

            Log.d(TAG, "Firebase Database initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase Database", e);
        }
    }
}
