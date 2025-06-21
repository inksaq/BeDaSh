package com.bedash.app.Education;
import com.bedash.app.BaseActivity;
import com.bedash.app.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends BaseActivity {
    private TextView questionText;
    private RadioGroup answerGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton;
    private ImageButton backButton;
    private TextView progressText;

    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String quizTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        setupBase();

        // Get quiz title from intent
        quizTitle = getIntent().getStringExtra("quiz_title");
        if (quizTitle == null) quizTitle = "Quiz";

        initializeViews();
        setupQuestions();
        displayCurrentQuestion();
        setupNextButton();
        setupButton();
    }

    private void initializeViews() {
        questionText = findViewById(R.id.question_text);
        answerGroup = findViewById(R.id.answer_group);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);
        progressText = findViewById(R.id.progress_text);

        TextView quizTitleView = findViewById(R.id.quiz_title);
        quizTitleView.setText(quizTitle);
    }

    private void setupQuestions() {
        questions = new ArrayList<>();

        if (quizTitle.contains("Nutrition")) {
            setupNutritionQuestions();
        } else if (quizTitle.contains("Exercise")) {
            setupExerciseQuestions();
        } else {
            setupGeneralQuestions();
        }
    }

    private void setupNutritionQuestions() {
        questions.add(new QuizQuestion(
                "How many calories are in 1 gram of protein?",
                new String[]{"4 calories", "7 calories", "9 calories", "2 calories"},
                0
        ));

        questions.add(new QuizQuestion(
                "What is the recommended daily protein intake for an active adult?",
                new String[]{"0.5g per kg body weight", "0.8g per kg body weight", "1.2-2.0g per kg body weight", "3.0g per kg body weight"},
                2
        ));

        questions.add(new QuizQuestion(
                "Which macronutrient provides the most calories per gram?",
                new String[]{"Protein", "Carbohydrates", "Fat", "Alcohol"},
                2
        ));

        questions.add(new QuizQuestion(
                "What does BMR stand for?",
                new String[]{"Basic Metabolic Rate", "Basal Metabolic Rate", "Body Mass Ratio", "Biological Muscle Response"},
                1
        ));

        questions.add(new QuizQuestion(
                "Which vitamin is primarily synthesized by sun exposure?",
                new String[]{"Vitamin A", "Vitamin C", "Vitamin D", "Vitamin K"},
                2
        ));
    }

    private void setupExerciseQuestions() {
        questions.add(new QuizQuestion(
                "What is the recommended frequency for strength training?",
                new String[]{"Every day", "2-3 times per week", "Once per week", "5-6 times per week"},
                1
        ));

        questions.add(new QuizQuestion(
                "Which energy system is primarily used during high-intensity, short-duration activities?",
                new String[]{"Aerobic system", "Phosphocreatine system", "Glycolytic system", "Oxidative system"},
                1
        ));

        questions.add(new QuizQuestion(
                "What is the principle of progressive overload?",
                new String[]{"Doing the same workout repeatedly", "Gradually increasing training demands", "Only increasing weight", "Training to failure every session"},
                1
        ));
    }

    private void setupGeneralQuestions() {
        questions.add(new QuizQuestion(
                "What is the most important factor in weight loss?",
                new String[]{"Exercise type", "Meal timing", "Caloric deficit", "Supplement use"},
                2
        ));

        questions.add(new QuizQuestion(
                "How much water should an average adult drink daily?",
                new String[]{"1-2 liters", "2-3 liters", "4-5 liters", "0.5-1 liter"},
                1
        ));
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex < questions.size()) {
            QuizQuestion question = questions.get(currentQuestionIndex);
            questionText.setText(question.getQuestion());

            String[] options = question.getOptions();
            option1.setText(options[0]);
            option2.setText(options[1]);
            option3.setText(options[2]);
            option4.setText(options[3]);

            answerGroup.clearCheck();
            progressText.setText("Question " + (currentQuestionIndex + 1) + " of " + questions.size());

            if (currentQuestionIndex == questions.size() - 1) {
                nextButton.setText("Finish Quiz");
            }
        }
    }
    private void setupButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupNextButton() {
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = answerGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(QuizActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check answer
                int selectedAnswer = -1;
                if (selectedId == R.id.option1) selectedAnswer = 0;
                else if (selectedId == R.id.option2) selectedAnswer = 1;
                else if (selectedId == R.id.option3) selectedAnswer = 2;
                else if (selectedId == R.id.option4) selectedAnswer = 3;

                if (selectedAnswer == questions.get(currentQuestionIndex).getCorrectAnswer()) {
                    score++;
                }

                currentQuestionIndex++;

                if (currentQuestionIndex < questions.size()) {
                    displayCurrentQuestion();
                } else {
                    showResults();
                }
            }
        });
    }

    private void showResults() {
        Intent intent = new Intent(QuizActivity.this, QuizResultsActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total_questions", questions.size());
        intent.putExtra("quiz_title", quizTitle);
        startActivity(intent);
        finish();
    }

    // Inner class for quiz questions
    private static class QuizQuestion {
        private String question;
        private String[] options;
        private int correctAnswer;

        public QuizQuestion(String question, String[] options, int correctAnswer) {
            this.question = question;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        public String getQuestion() { return question; }
        public String[] getOptions() { return options; }
        public int getCorrectAnswer() { return correctAnswer; }
    }
}