package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Base Activity that includes the navigation bar and common functionality
 * for screens that appear after login.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Firebase
    protected FirebaseAuth mAuth;
    protected FirestoreManager mFirestoreManager;

    // Navbar elements
    protected TextView userTextView;
    protected ImageButton homeButton;
    protected ImageButton navbarLogoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Get Firebase instances from Application
        mAuth = BeDashApplication.getInstance().getAuth();
        mFirestoreManager = BeDashApplication.getInstance().getFirestoreManager();

    }


    protected void setupBase() {
        // Init navbar components
        initNavbar();

        // Check if user is logged in
        checkUserAuthStatus();

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initNavbar() {
        // Only initialize if the navbar is included in the layout
        try {
            userTextView = findViewById(R.id.user_text_view);
            homeButton = findViewById(R.id.home_button);
            navbarLogoutButton = findViewById(R.id.navbar_logout_button);

            // Setup navbar buttons
            if (homeButton != null) {
                homeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleHomeClick();
                    }
                });
            }

            if (navbarLogoutButton != null) {
                navbarLogoutButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logout();
                    }
                });
            }
        } catch (Exception e) {
            // Navbar not found in this layout
        }
    }

    protected void handleHomeClick() {
        // Default behavior - go to dashboard if not already there
        if (!(this instanceof DashboardActivity)) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            // Already on dashboard, show toast
            Toast.makeText(this, "Already on Home screen", Toast.LENGTH_SHORT).show();
        }
    }


    protected void logout() {
        if (mAuth != null) {
            // Mark user as offline in database
            BeDashApplication.getInstance().setUserOffline();

            // Sign out
            mAuth.signOut();
            navigateToLogin();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserAuthStatus() {
        // Check auth status
        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is signed in, update UI
                updateUIWithUser(currentUser);

                // Update last login time and online status
                String nurseId = currentUser.getUid();
                mFirestoreManager.updateNurseLastLogin(nurseId);
                BeDashApplication.getInstance().setUserOnline();
            } else {
                // No user is signed in, go to login screen
                navigateToLogin();
            }
        }
    }

    private void updateUIWithUser(FirebaseUser user) {
        // Display signed-in user's information in navbar
        if (user != null && userTextView != null) {
            userTextView.setText(user.getEmail());
        }
    }

    protected void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close current activity to prevent going back
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Add null check before accessing mAuth
        if (mAuth != null) {
            // Check if user is signed in when activity starts
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // User is not signed in, go to login
                navigateToLogin();
            } else {
                // User is signed in, set online status
                BeDashApplication.getInstance().setUserOnline();
            }
        } else {
            // In case mAuth is still null here, initialize it from application
            mAuth = BeDashApplication.getInstance().getAuth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set online status when activity resumes
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            BeDashApplication.getInstance().setUserOnline();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Set offline status if this is the last activity being destroyed
        if (isFinishing() && mAuth != null && mAuth.getCurrentUser() != null) {
            BeDashApplication.getInstance().setUserOffline();
        }
        super.onDestroy();
    }
}