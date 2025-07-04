package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * IMPORTANT: Changed to extend AppCompatActivity instead of BaseActivity
 * to avoid authentication flow issues during login
 */
public class LoginActivity extends AppCompatActivity {

    // UI Elements
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerButton;
    private ProgressBar progressBar;

    // Firebase
    FirebaseAuth mAuth;
    private FirestoreManager mFirestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = BeDashApplication.getInstance().getAuth();
        mFirestoreManager = FirestoreManager.getInstance();

        // Initialize UI components
        initializeViews();

        // Set up button listeners
        setupButtonListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is already signed in
        if (mAuth.getCurrentUser() != null) {
            // User is already signed in, go to Dashboard
            navigateToDashboard();
        }
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupButtonListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter email and password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // Sign in with email and password
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success

                                    // Update last login time and online status
                                    String nurseId = mAuth.getCurrentUser().getUid();
                                    mFirestoreManager.updateNurseLastLogin(nurseId);
                                    BeDashApplication.getInstance().setUserOnline();

                                    // Navigate to dashboard
                                    navigateToDashboard();
                                } else {
                                    // Sign in fails, display a message to the user
                                    Toast.makeText(LoginActivity.this, "Authentication failed: " +
                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Close the login activity
    }
}