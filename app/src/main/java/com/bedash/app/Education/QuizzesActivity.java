package com.bedash.app.Education;

import com.bedash.app.BaseActivity;
import com.bedash.app.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuizzesActivity extends BaseActivity {
    private ListView quizListView;
    private ArrayList<Map<String, String>> quizList;
    private SimpleAdapter adapter;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizzes);
        setupBase();
        initializeViews();
        setupQuizList();
        setupbutton();
    }

    private void initializeViews() {
        quizListView = findViewById(R.id.quiz_list_view);
        backButton = findViewById(R.id.back_button);
    }

    private void setupbutton(){
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupQuizList() {
        quizList = new ArrayList<>();

        // Add different quiz categories
        addQuiz("Nutrition Fundamentals", "Test your knowledge of basic nutrition principles", "5 questions", "Beginner");
        addQuiz("Exercise Physiology", "Understanding how the body responds to exercise", "7 questions", "Intermediate");
        addQuiz("Client Assessment", "Best practices for evaluating new clients", "6 questions", "Intermediate");
        addQuiz("Macronutrient Balance", "Optimal protein, carbs, and fat distribution", "5 questions", "Advanced");
        addQuiz("Weight Management", "Science-based approaches to weight loss and gain", "8 questions", "Intermediate");
        addQuiz("Special Populations", "Training considerations for different groups", "6 questions", "Advanced");
        addQuiz("Supplement Science", "Evidence-based supplement recommendations", "5 questions", "Advanced");
        addQuiz("Behavior Change", "Psychology of habit formation and motivation", "7 questions", "Intermediate");

        // Set up adapter
        adapter = new SimpleAdapter(
                this,
                quizList,
                R.layout.quiz_list_item,
                new String[]{"title", "description", "questions", "level"},
                new int[]{R.id.quiz_title, R.id.quiz_description, R.id.quiz_questions, R.id.quiz_level}
        );

        quizListView.setAdapter(adapter);

        // Set item click listener
        quizListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> selectedQuiz = quizList.get(position);
                startQuiz(selectedQuiz);
            }
        });
    }

    private void addQuiz(String title, String description, String questions, String level) {
        Map<String, String> quiz = new HashMap<>();
        quiz.put("title", title);
        quiz.put("description", description);
        quiz.put("questions", questions);
        quiz.put("level", level);
        quizList.add(quiz);
    }

    private void startQuiz(Map<String, String> quiz) {
        Intent intent = new Intent(QuizzesActivity.this, QuizActivity.class);
        intent.putExtra("quiz_title", quiz.get("title"));
        intent.putExtra("quiz_description", quiz.get("description"));
        intent.putExtra("quiz_level", quiz.get("level"));
        startActivity(intent);
    }
}