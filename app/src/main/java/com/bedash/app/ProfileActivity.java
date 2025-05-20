package com.bedash.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends BaseActivity {
    private static final String TAG = "ProfileActivity";

    private TextView profileContentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup base activity (navbar, auth, etc)
        setupBase();

        // Initialize profile-specific views
        profileContentText = findViewById(R.id.profile_content_text);

        // Load profile data
        loadProfileData();
    }

    private void loadProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String email = currentUser.getEmail();

            // Default profile info with email
            StringBuilder profileInfo = new StringBuilder();
            profileInfo.append("Profile Information\n\n");
            profileInfo.append("Email: ").append(email).append("\n");

            // Set initial profile info while we load from database
            profileContentText.setText(profileInfo.toString());

            // Get nurse data from database using FirestoreManager
            mFirestoreManager.getNurseProfile(uid, new FirestoreManager.DatabaseCallback<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot document) {
                    if (document.exists()) {
                        // Update profile info with database data
                        profileInfo.append("\n");

                        // Add name fields
                        if (document.contains("firstName") && document.contains("lastName")) {
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            profileInfo.append("Name: ").append(firstName).append(" ").append(lastName).append("\n\n");
                        }

                        // Handle createdAt timestamp if available
                        if (document.contains("createdAt")) {
                            com.google.firebase.Timestamp createdAt = document.getTimestamp("createdAt");
                            if (createdAt != null) {
                                String formattedDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                                        .format(createdAt.toDate());
                                profileInfo.append("Member since: ").append(formattedDate).append("\n");
                            }
                        }

                        // Last login time
                        if (document.contains("lastLogin")) {
                            com.google.firebase.Timestamp lastLogin = document.getTimestamp("lastLogin");
                            if (lastLogin != null) {
                                String formattedDate = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                        .format(lastLogin.toDate());
                                profileInfo.append("Last login: ").append(formattedDate).append("\n");
                            }
                        }

                        // Check online status
                        if (document.contains("status")) {
                            com.google.firebase.firestore.DocumentSnapshot statusDoc = document;
                            Object statusObj = statusDoc.get("status");

                            if (statusObj instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> status = (java.util.Map<String, Object>) statusObj;
                                Boolean isOnline = (Boolean) status.get("online");
                                profileInfo.append("Status: ").append(isOnline != null && isOnline ? "Online" : "Offline");
                            } else {
                                profileInfo.append("Status: Unknown");
                            }
                        } else {
                            profileInfo.append("Status: Offline");
                        }

                        // Set updated profile info
                        profileContentText.setText(profileInfo.toString());
                    }
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Error loading profile data", error);
                    Toast.makeText(ProfileActivity.this,
                            "Error loading profile data: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}