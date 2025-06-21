package com.bedash.app.Education;
import com.bedash.app.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bedash.app.Education.QuizzesActivity;
import com.bedash.app.R;


public class QuizResultsActivity extends BaseActivity {
    private TextView quizTitleText;
    private TextView scoreText;
    private TextView percentageText;
    private TextView feedbackText;
    private ProgressBar scoreProgressBar;
    private Button retakeButton;
    private Button continueButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_results);
        setupBase();

        initializeViews();
        displayResults();
        setupButtons();
    }

    private void initializeViews() {
        quizTitleText = findViewById(R.id.quiz_title_text);
        scoreText = findViewById(R.id.score_text);
        percentageText = findViewById(R.id.percentage_text);
        feedbackText = findViewById(R.id.feedback_text);
        scoreProgressBar = findViewById(R.id.score_progress_bar);
        retakeButton = findViewById(R.id.retake_button);
        continueButton = findViewById(R.id.continue_button);
        backButton = findViewById(R.id.back_button);
    }

    private void displayResults() {
        // Get results from intent
        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("total_questions", 5);
        String quizTitle = getIntent().getStringExtra("quiz_title");

        // Calculate percentage
        int percentage = (score * 100) / totalQuestions;

        // Display results
        quizTitleText.setText(quizTitle);
        scoreText.setText(score + " / " + totalQuestions);
        percentageText.setText(percentage + "%");
        scoreProgressBar.setProgress(percentage);

        // Provide feedback based on score
        String feedback = getFeedbackMessage(percentage);
        feedbackText.setText(feedback);
    }

    private String getFeedbackMessage(int percentage) {
        if (percentage >= 90) {
            return "🌟 Excellent! You have a strong understanding of this topic. Keep up the great work!";
        } else if (percentage >= 75) {
            return "👍 Good job! You have a solid grasp of the material with room for minor improvements.";
        } else if (percentage >= 60) {
            return "📚 Not bad! Consider reviewing the material and trying again to improve your understanding.";
        } else {
            return "💪 Keep learning! Review the tutorial materials and don't give up - practice makes perfect!";
        }
    }

    private void setupButtons() {
        retakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to quiz with same title
                String quizTitle = getIntent().getStringExtra("quiz_title");
                Intent intent = new Intent(QuizResultsActivity.this, QuizActivity.class);
                intent.putExtra("quiz_title", quizTitle);
                startActivity(intent);
                finish();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to quizzes list
                Intent intent = new Intent(QuizResultsActivity.this, QuizzesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}