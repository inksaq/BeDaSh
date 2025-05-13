package com.bedash.app;

import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends BaseActivity {
    private TextView profileContentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup base activity (navbar, auth, etc)
        setupBase();

        // Initialize profile-specific views
        profileContentText = findViewById(R.id.profile_content_text);

        // Set profile content
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String displayText = "Profile Information\n\n" +
                    "Email: " + email + "\n" +
                    "Member since: May 2025\n" +
                    "Status: Active";
            profileContentText.setText(displayText);
        }
    }
}