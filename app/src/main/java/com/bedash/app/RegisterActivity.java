package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * IMPORTANT: Changed to extend AppCompatActivity instead of BaseActivity
 * to avoid authentication flow issues during registration
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;
    private Button backToLoginButton;
    private ProgressBar progressBar;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirestoreManager mFirestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        mAuth = BeDashApplication.getInstance().getAuth();
        mFirestoreManager = FirestoreManager.getInstance();

        // Initialize UI components
        initializeViews();

        // Set up button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        registerButton = findViewById(R.id.register_button);
        backToLoginButton = findViewById(R.id.home_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupButtonListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least 6 characters",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // Use the enhanced registration method
                mFirestoreManager.registerNurseWithFirebase(email, password,
                        new FirestoreManager.DatabaseCallback<String>() {
                            @Override
                            public void onSuccess(String nurseId) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this,
                                        "Account created successfully!",
                                        Toast.LENGTH_SHORT).show();

                                // Set online status
                                BeDashApplication.getInstance().setUserOnline();

                                // Go to DashboardActivity
                                Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                startActivity(intent);
                                finish(); // Close the register activity
                            }

                            @Override
                            public void onError(Exception error) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this,
                                        "Registration failed: " + error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        if (backToLoginButton != null) {
            backToLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go back to LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}