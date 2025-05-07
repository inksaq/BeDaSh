package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout mainContentLayout;
    private TextView statusTextView;
    private Button logoutButton;

    // Firebase auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Init Firebase singleton
        mAuth = FirebaseAuth.getInstance();

        // Init view components
        initializeViews();

        // Do some google/android magic
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Show splash screen for a short time, then check auth
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserAuthStatus();
            }
        }, 1500); // 1.5 second splash screen

        // Setup logout button
        setupLogoutButton();
    }

    private void initializeViews() {
        mainContentLayout = findViewById(R.id.main_content_layout);
        statusTextView = findViewById(R.id.status_text_view);
        logoutButton = findViewById(R.id.logout_button);
    }

    private void setupLogoutButton() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                navigateToLogin();
                Toast.makeText(MainActivity.this, "Logged out successfully",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserAuthStatus() {
        // Check auth status
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, show main content
            updateUIWithUser(currentUser);
        } else {
            // No user is signed in, go to login screen
            navigateToLogin();
        }
    }

    private void updateUIWithUser(FirebaseUser user) {
        mainContentLayout.setVisibility(View.VISIBLE);

        // Display signed-in user's information
        if (user != null) {
            statusTextView.setText("Logged in as: " + user.getEmail());
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity to prevent going back
    }

    @Override
    public void onStart() {
        super.onStart();
        // This will be called after onCreate when activity starts
        // No need to duplicate auth check here as we already do it after splash screen
    }
}