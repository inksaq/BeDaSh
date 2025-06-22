package com.bedash.app.Education;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bedash.app.BaseActivity;
import com.bedash.app.R;

public class EducationActivity extends BaseActivity {
    private Button tutorialsButton;
    private Button quizzesButton;
    private Button scenariosButton;
    private Button progressButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_education);
        setupBase();
        initializeViews();
        setupButtons();
    }

    private void initializeViews() {
        tutorialsButton = findViewById(R.id.tutorials_button);
        quizzesButton = findViewById(R.id.quizzes_button);
        scenariosButton = findViewById(R.id.scenarios_button);
        progressButton = findViewById(R.id.progress_button);
        backButton = findViewById(R.id.back_button);
    }

    private void setupButtons() {
        tutorialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EducationActivity.this, "Opening Tutorials", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EducationActivity.this, TutorialsActivity.class);
                startActivity(intent);
            }
        });

        quizzesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EducationActivity.this, "Opening Quizzes", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EducationActivity.this, QuizzesActivity.class);
                startActivity(intent);
            }
        });

        scenariosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EducationActivity.this, "Opening Scenarios", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EducationActivity.this, ScenariosActivity.class);
                startActivity(intent);
            }
        });

        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EducationActivity.this, "Opening Learning Progress", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EducationActivity.this, LearningProgressActivity.class);
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}