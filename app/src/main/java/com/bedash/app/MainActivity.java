package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    // Splash screen delay
    private static final int SPLASH_DELAY = 1500; // 1.5 seconds
    private boolean isRedirecting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Note: We don't call setupBase() here because we want to handle auth differently for splash screen

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

    @Override
    protected void navigateToLogin() {
        // Override to prevent the base class from navigating during splash
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity to prevent going back
    }

    // Override these methods to prevent base class navigation during splash
    @Override
    protected void setupBase() {
        // Don't call super.setupBase() as we're handling navigation differently
    }

    @Override
    protected void onStart() {
        // Don't call super.onStart() as we're handling auth check differently
        super.onStart();
    }
}