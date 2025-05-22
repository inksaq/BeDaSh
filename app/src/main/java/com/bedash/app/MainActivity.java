package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    // Splash screen delay
    private static final int SPLASH_DELAY = 1500; // 1.5 seconds
    private boolean isRedirecting = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = BeDashApplication.getInstance().getAuth();

        // Show splash screen for a short time, then check auth
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserAuthStatus();
            }
        }, SPLASH_DELAY);
    }

    private void checkUserAuthStatus() {
        if (isRedirecting) return; // Prevent multiple redirects
        isRedirecting = true;

        if (mAuth != null && mAuth.getCurrentUser() != null) {
            // User is signed in, go to dashboard
            navigateToDashboard();
        } else {
            // No user is signed in, go to login screen
            navigateToLogin();
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity to prevent going back
    }

    protected void navigateToLogin() {
        // Override to prevent the base class from navigating during splash
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity to prevent going back
    }

}