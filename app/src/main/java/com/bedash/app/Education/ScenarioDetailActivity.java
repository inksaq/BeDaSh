package com.bedash.app.Education;

import com.bedash.app.BaseActivity;
import com.bedash.app.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ScenarioDetailActivity extends BaseActivity {
    private TextView scenarioTitleText;
    private TextView scenarioLevelText;
    private TextView scenarioDurationText;
    private TextView scenarioDescriptionText;
    private TextView scenarioDetailsText;
    private TextView challengeText;
    private Button startScenarioButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenario_detail);
        setupBase();

        initializeViews();
        loadScenarioContent();
        setupButtons();
    }

    private void initializeViews() {
        scenarioTitleText = findViewById(R.id.scenario_title_text);
        scenarioLevelText = findViewById(R.id.scenario_level_text);
        scenarioDurationText = findViewById(R.id.scenario_duration_text);
        scenarioDescriptionText = findViewById(R.id.scenario_description_text);
        scenarioDetailsText = findViewById(R.id.scenario_details_text);
        challengeText = findViewById(R.id.challenge_text);
        startScenarioButton = findViewById(R.id.start_scenario_button);
        backButton = findViewById(R.id.back_button);
    }

    private void loadScenarioContent() {
        // Get scenario data from intent
        String title = getIntent().getStringExtra("scenario_title");
        String description = getIntent().getStringExtra("scenario_description");
        String level = getIntent().getStringExtra("scenario_level");
        String duration = getIntent().getStringExtra("scenario_duration");

        // Set basic info
        scenarioTitleText.setText(title);
        scenarioLevelText.setText(level);
        scenarioDurationText.setText(duration);
        scenarioDescriptionText.setText(description);

        // Load detailed content based on scenario title
        loadScenarioDetails(title);
    }

    private void loadScenarioDetails(String title) {
        String details = "";
        String challenge = "";

        switch (title) {
            case "The Plateau Client":
                details = "Sarah, 32, marketing manager\n\n" +
                        "Background:\n" +
                        "• Started program 3 months ago at 150 lbs\n" +
                        "• Lost 12 lbs in first 2 months\n" +
                        "• No progress in past 4 weeks\n" +
                        "• Following 1,400 calorie diet\n" +
                        "• Exercises 4x/week (3 cardio, 1 strength)\n" +
                        "• Feeling frustrated and demotivated\n\n" +
                        "Current metrics:\n" +
                        "• Weight: 138 lbs (no change in 4 weeks)\n" +
                        "• Body fat: ~25%\n" +
                        "• Energy levels: Low\n" +
                        "• Sleep: 6-7 hours/night";

                challenge = "Your Challenge:\n\n" +
                        "1. Identify potential causes of the plateau\n" +
                        "2. Develop a strategy to break through\n" +
                        "3. Address her motivation concerns\n" +
                        "4. Create a modified program\n" +
                        "5. Set realistic expectations for next phase";
                break;

            case "The Injured Athlete":
                details = "Marcus, 24, competitive runner\n\n" +
                        "Background:\n" +
                        "• Marathon runner, 3:15 PR\n" +
                        "• Recent knee injury (patellofemoral pain)\n" +
                        "• Cleared for low-impact activity\n" +
                        "• Race in 4 months he wants to do\n" +
                        "• Currently very frustrated\n" +
                        "• Usually runs 60-70 miles/week\n\n" +
                        "Current status:\n" +
                        "• Pain level: 3/10 with running\n" +
                        "• Can bike and swim pain-free\n" +
                        "• Strength training cleared\n" +
                        "• Very motivated but impatient";

                challenge = "Your Challenge:\n\n" +
                        "1. Design safe training program\n" +
                        "2. Maintain cardiovascular fitness\n" +
                        "3. Address nutritional needs during recovery\n" +
                        "4. Manage his expectations\n" +
                        "5. Create return-to-running protocol";
                break;

            case "The Busy Executive":
                details = "Jennifer, 42, CEO\n\n" +
                        "Background:\n" +
                        "• Works 60+ hours/week\n" +
                        "• Travels 2-3 days per week\n" +
                        "• Has only 30 min, 3x/week for exercise\n" +
                        "• Often eats meals at restaurants\n" +
                        "• High stress levels\n" +
                        "• Previous fitness experience\n\n" +
                        "Goals:\n" +
                        "• Lose 15 lbs\n" +
                        "• Increase energy\n" +
                        "• Manage stress better\n" +
                        "• Fit into clothes from 2 years ago";

                challenge = "Your Challenge:\n\n" +
                        "1. Design ultra-efficient workout plan\n" +
                        "2. Create travel-friendly nutrition strategy\n" +
                        "3. Provide restaurant eating guidelines\n" +
                        "4. Address stress management\n" +
                        "5. Make program sustainable long-term";
                break;

            default:
                details = "Scenario details will be loaded here based on the selected case study.";
                challenge = "Complete the scenario to unlock the full challenge details.";
        }

        scenarioDetailsText.setText(details);
        challengeText.setText(challenge);
    }

    private void setupButtons() {
        startScenarioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ScenarioDetailActivity.this,
                        "Interactive scenario practice coming soon!",
                        Toast.LENGTH_LONG).show();

                // In a full implementation, this would start an interactive scenario
                // For now, we'll just show a success message
                Toast.makeText(ScenarioDetailActivity.this,
                        "Scenario completed! +50 points earned",
                        Toast.LENGTH_SHORT).show();
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