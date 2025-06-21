package com.bedash.app.Education;
import com.bedash.app.BaseActivity;
import com.bedash.app.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LearningProgressActivity extends BaseActivity {
    private TextView overallProgressText;
    private ProgressBar overallProgressBar;
    private TextView streakText;
    private TextView totalPointsText;
    private ListView achievementsListView;
    private ListView recentActivityListView;
    private ImageButton backButton;

    private ArrayList<Map<String, String>> achievementsList;
    private ArrayList<Map<String, String>> recentActivityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_progress);
        setupBase();
        initializeViews();
        loadProgressData();
        setupLists();
        setupButtons();
    }

    private void initializeViews() {
        overallProgressText = findViewById(R.id.overall_progress_text);
        overallProgressBar = findViewById(R.id.overall_progress_bar);
        streakText = findViewById(R.id.streak_text);
        totalPointsText = findViewById(R.id.total_points_text);
        achievementsListView = findViewById(R.id.achievements_list_view);
        recentActivityListView = findViewById(R.id.recent_activity_list_view);
        backButton = findViewById(R.id.back_button);
    }

    private void loadProgressData() {
        // Simulate user progress data (in a real app, this would come from a database)
        int completedModules = 8;
        int totalModules = 15;
        int progressPercentage = (completedModules * 100) / totalModules;

        overallProgressText.setText(progressPercentage + "% Complete (" + completedModules + "/" + totalModules + " modules)");
        overallProgressBar.setProgress(progressPercentage);

        streakText.setText("🔥 5 day learning streak!");
        totalPointsText.setText("⭐ 1,250 total points earned");
    }

    private void setupLists() {
        setupAchievements();
        setupRecentActivity();
    }

    private void setupButtons() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void setupAchievements() {
        achievementsList = new ArrayList<>();

        // Add achievements (earned and unearned)
        addAchievement("First Steps", "Complete your first tutorial", "✅ Unlocked", "#4CAF50");
        addAchievement("Quiz Master", "Score 100% on any quiz", "✅ Unlocked", "#4CAF50");
        addAchievement("Knowledge Seeker", "Complete 5 tutorials", "✅ Unlocked", "#4CAF50");
        addAchievement("Consistent Learner", "Learn for 5 days in a row", "✅ Unlocked", "#4CAF50");
        addAchievement("Scenario Expert", "Complete 3 practice scenarios", "🔒 Locked", "#999999");
        addAchievement("Perfect Score", "Get 100% on 3 different quizzes", "🔒 Locked", "#999999");
        addAchievement("Nutrition Expert", "Complete all nutrition modules", "🔒 Locked", "#999999");
        addAchievement("Master Trainer", "Complete all available content", "🔒 Locked", "#999999");

        SimpleAdapter achievementsAdapter = new SimpleAdapter(
                this,
                achievementsList,
                R.layout.achievement_list_item,
                new String[]{"title", "description", "status", "color"},
                new int[]{R.id.achievement_title, R.id.achievement_description, R.id.achievement_status, R.id.achievement_icon}
        );

        achievementsListView.setAdapter(achievementsAdapter);
    }

    private void setupRecentActivity() {
        recentActivityList = new ArrayList<>();

        // Add recent learning activities
        addRecentActivity("Completed Quiz", "Nutrition Fundamentals - 80% score", "2 hours ago");
        addRecentActivity("Finished Tutorial", "BMR & TDEE Calculations", "Yesterday");
        addRecentActivity("Practiced Scenario", "The Plateau Client", "2 days ago");
        addRecentActivity("Completed Quiz", "Exercise Physiology - 100% score", "3 days ago");
        addRecentActivity("Finished Tutorial", "Client Assessment Techniques", "4 days ago");

        SimpleAdapter recentActivityAdapter = new SimpleAdapter(
                this,
                recentActivityList,
                R.layout.recent_activity_item,
                new String[]{"activity_type", "activity_name", "time_ago"},
                new int[]{R.id.activity_type, R.id.activity_name, R.id.activity_time}
        );

        recentActivityListView.setAdapter(recentActivityAdapter);
    }

    private void addAchievement(String title, String description, String status, String color) {
        Map<String, String> achievement = new HashMap<>();
        achievement.put("title", title);
        achievement.put("description", description);
        achievement.put("status", status);
        achievement.put("color", color);
        achievementsList.add(achievement);
    }

    private void addRecentActivity(String activityType, String activityName, String timeAgo) {
        Map<String, String> activity = new HashMap<>();
        activity.put("activity_type", activityType);
        activity.put("activity_name", activityName);
        activity.put("time_ago", timeAgo);
        recentActivityList.add(activity);
    }
}