package com.bedash.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

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

    @SuppressLint("WrongViewCast")
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
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // Step 1: Create user with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser(); // Or task.getResult().getUser();
                                    if (firebaseUser != null) {
                                        String userId = firebaseUser.getUid();

                                        // Step 2: Create nurse profile in Firestore using the UID
                                        Map<String, Object> otherDetails = new HashMap<>();
                                        // Add any other details you collect during registration here
                                        // e.g., otherDetails.put("fullName", "Nurse Name");

                                        mFirestoreManager.createNurseProfile(userId, email, otherDetails,
                                                new FirestoreManager.DatabaseCallback<Void>() {
                                                    @Override
                                                    public void onSuccess(Void result) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                                                        BeDashApplication.getInstance().setUserOnline();

                                                        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }

                                                    public void onError(Exception error) {
                                                        progressBar.setVisibility(View.GONE);
                                                        // If Firestore profile creation fails, the Auth user still exists.
                                                        Toast.makeText(RegisterActivity.this, "Registration failed (profile creation): " + error.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        // Should not happen if task is successful
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(RegisterActivity.this, "Registration failed: User is null after auth.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    // Authentication creation failed
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        if (backToLoginButton != null) {
            backToLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}