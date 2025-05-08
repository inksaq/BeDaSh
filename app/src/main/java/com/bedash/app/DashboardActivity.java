package com.bedash.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DashboardActivity extends BaseActivity {
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Setup base activity (navbar, auth, etc)
        setupBase();

        // Init additional view components
        initializeViews();

        // Setup additional buttons
        setupButtons();
    }

    private void initializeViews() {
        logoutButton = findViewById(R.id.logout_button);
    }

    private void setupButtons() {
        // Setup bottom logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    @Override
    protected void handleHomeClick() {
        // We're already on the dashboard, just show a toast
        Toast.makeText(this, "Already on Home screen", Toast.LENGTH_SHORT).show();
    }
}