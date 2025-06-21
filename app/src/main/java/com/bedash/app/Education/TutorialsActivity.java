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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TutorialsActivity extends BaseActivity {
    private ListView tutorialListView;
    private ArrayList<Map<String, String>> tutorialList;
    private SimpleAdapter adapter;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorials);
        setupBase();
        initializeViews();
        setupTutorialList();
        setupButton();
    }

    private void initializeViews() {
        tutorialListView = findViewById(R.id.tutorial_list_view);
        backButton = findViewById(R.id.back_button);
    }

    private void setupButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupTutorialList() {
        tutorialList = new ArrayList<>();

        // Add tutorial topics
        addTutorial("Nutrition Basics", "Understanding macronutrients, micronutrients, and calorie balance", "beginner", "15 min");
        addTutorial("BMR & TDEE Calculations", "Learn to calculate Basal Metabolic Rate and Total Daily Energy Expenditure", "beginner", "20 min");
        addTutorial("Macronutrient Ratios", "Setting optimal protein, carbs, and fat percentages for different goals", "intermediate", "25 min");
        addTutorial("Client Assessment", "Comprehensive techniques for evaluating new clients", "intermediate", "30 min");
        addTutorial("Goal Setting Strategies", "SMART goals and sustainable habit formation", "beginner", "18 min");
        addTutorial("Exercise Programming", "Creating effective workout plans for different populations", "advanced", "35 min");
        addTutorial("Dietary Restrictions", "Working with allergies, intolerances, and special diets", "intermediate", "22 min");
        addTutorial("Progress Tracking", "Methods for monitoring and adjusting client programs", "intermediate", "20 min");
        addTutorial("Motivation Techniques", "Psychology of behavior change and client adherence", "advanced", "28 min");
        addTutorial("Supplement Science", "Evidence-based supplement recommendations", "advanced", "25 min");

        // Set up adapter
        adapter = new SimpleAdapter(
                this,
                tutorialList,
                R.layout.tutorial_list_item,
                new String[]{"title", "description", "level", "duration"},
                new int[]{R.id.tutorial_title, R.id.tutorial_description, R.id.tutorial_level, R.id.tutorial_duration}
        );

        tutorialListView.setAdapter(adapter);

        // Set item click listener
        tutorialListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> selectedTutorial = tutorialList.get(position);
                openTutorial(selectedTutorial);
            }
        });
    }

    private void addTutorial(String title, String description, String level, String duration) {
        Map<String, String> tutorial = new HashMap<>();
        tutorial.put("title", title);
        tutorial.put("description", description);
        tutorial.put("level", level.toUpperCase());
        tutorial.put("duration", duration);
        tutorialList.add(tutorial);
    }

    private void openTutorial(Map<String, String> tutorial) {
        Intent intent = new Intent(TutorialsActivity.this, TutorialContentActivity.class);
        intent.putExtra("tutorial_title", tutorial.get("title"));
        intent.putExtra("tutorial_description", tutorial.get("description"));
        intent.putExtra("tutorial_level", tutorial.get("level"));
        intent.putExtra("tutorial_duration", tutorial.get("duration"));
        startActivity(intent);
    }
}