package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
        private ConstraintLayout splashLayout;
        private ConstraintLayout loginLayout;
        private ConstraintLayout mainContentLayout;
        private EditText emailEditText;
        private EditText passwordEditText;
        private Button loginButton;
        private Button registerButton;
        private Button logoutButton;
        private TextView statusTextView;
        private ProgressBar progressBar;

        // firebase auth
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_main);

            // init firebase singleton
            mAuth = FirebaseAuth.getInstance();

            // init view components
            initializeViews();

            // do some google/android magic
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });

            // show splashscreen
            showSplashScreen();

            // setup buttons
            setupButtonListeners();
        }

        private void initializeViews() {
            splashLayout = findViewById(R.id.splash_layout);
            loginLayout = findViewById(R.id.login_layout);
            mainContentLayout = findViewById(R.id.main_content_layout);
            emailEditText = findViewById(R.id.email_edit_text);
            passwordEditText = findViewById(R.id.password_edit_text);
            loginButton = findViewById(R.id.login_button);
            registerButton = findViewById(R.id.register_button);
            logoutButton = findViewById(R.id.logout_button);
            statusTextView = findViewById(R.id.status_text_view);
            progressBar = findViewById(R.id.progress_bar);
        }

        private void showSplashScreen() {
            splashLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            mainContentLayout.setVisibility(View.GONE);


            // add a delay
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // check auth
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        // check if signed in, show main screen
                        showMainContent(currentUser);
                    } else {
                        // check if no user is signed in, show login screen
                        showLoginScreen();
                    }
                }
            }, 1000); // 1 second delay
        }

        private void setupButtonListeners() {
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();

                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter email and password",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    // sign in with email and password
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // sign in success
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        showMainContent(user);
                                    } else {
                                        // sign in fails, display a message to the user
                                        Toast.makeText(MainActivity.this, "Authentication failed: " +
                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();

                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter email and password",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (password.length() < 6) {
                        Toast.makeText(MainActivity.this, "Password should be at least 6 characters",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    // new user with email and password
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // sign in success
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Toast.makeText(MainActivity.this, "Registration successful!",
                                                Toast.LENGTH_SHORT).show();
                                        showMainContent(user);
                                    } else {
                                        // registration fails, display a message to the user
                                        Toast.makeText(MainActivity.this, "Registration failed: " +
                                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    showLoginScreen();
                    Toast.makeText(MainActivity.this, "Logged out successfully",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void showLoginScreen() {
            splashLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
            mainContentLayout.setVisibility(View.GONE);
        }

        private void showMainContent(FirebaseUser user) {
            splashLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.GONE);
            mainContentLayout.setVisibility(View.VISIBLE);

            // signed-in user's information
            if (user != null) {
                statusTextView.setText("Logged in as: " + user.getEmail());
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            // Check signed in when app starts
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                showMainContent(currentUser);
            }
        }
}