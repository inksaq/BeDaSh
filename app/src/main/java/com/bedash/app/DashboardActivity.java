package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bedash.app.Education.EducationActivity;


public class DashboardActivity extends BaseActivity {
    private Button createClientButton;
    private Button viewExistingClientButton;
    private Button educationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        setupBase();
        initializeViews();
        setupButtons();
    }

    private void initializeViews() {
        createClientButton = findViewById(R.id.create_client_button);
        viewExistingClientButton = findViewById(R.id.view_existing_client_button);
        educationButton = findViewById(R.id.education_button);
    }

    private void setupButtons() {
        createClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DashboardActivity.this, "Create Client clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, CreateClientActivity.class);
                startActivity(intent);
            }
        });

        viewExistingClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DashboardActivity.this, "View Existing Clients clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, ClientListActivity.class);
                startActivity(intent);
            }
        });

        educationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(DashboardActivity.this, "Opening Education Center", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DashboardActivity.this, EducationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void handleHomeClick() {
        Toast.makeText(this, "Already on Home screen", Toast.LENGTH_SHORT).show();
    }
}