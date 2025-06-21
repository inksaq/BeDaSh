package com.bedash.app.Education;

import com.bedash.app.BaseActivity;
import com.bedash.app.R;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TutorialContentActivity extends BaseActivity {
    private TextView tutorialTitleText;
    private TextView tutorialLevelText;
    private TextView tutorialDurationText;
    private TextView stepNumberText;
    private TextView stepContentText;
    private ProgressBar tutorialProgressBar;
    private Button previousButton;
    private Button nextButton;
    private Button completeButton;
    private ImageButton backButton;

    private String[] tutorialSteps;
    private int currentStep = 0;
    private String tutorialTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_content);
        setupBase();

        // Get tutorial data from intent
        tutorialTitle = getIntent().getStringExtra("tutorial_title");
        String description = getIntent().getStringExtra("tutorial_description");
        String level = getIntent().getStringExtra("tutorial_level");
        String duration = getIntent().getStringExtra("tutorial_duration");

        initializeViews();
        setupTutorialData();
        displayCurrentStep();
        setupButtons();

        // Set tutorial info
        tutorialTitleText.setText(tutorialTitle);
        tutorialLevelText.setText(level);
        tutorialDurationText.setText(duration);
    }

    private void initializeViews() {
        tutorialTitleText = findViewById(R.id.tutorial_title_text);
        tutorialLevelText = findViewById(R.id.tutorial_level_text);
        tutorialDurationText = findViewById(R.id.tutorial_duration_text);
        stepNumberText = findViewById(R.id.step_number_text);
        stepContentText = findViewById(R.id.step_content_text);
        tutorialProgressBar = findViewById(R.id.tutorial_progress_bar);
        previousButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);
        completeButton = findViewById(R.id.complete_button);
        backButton = findViewById(R.id.back_button);
    }

    private void setupTutorialData() {
        // Load tutorial content based on title
        switch (tutorialTitle) {
            case "Nutrition Basics":
                tutorialSteps = new String[]{
                        "Welcome to Nutrition Basics!\n\nIn this tutorial, you'll learn the fundamentals of nutrition that every fitness trainer should know.\n\n• Macronutrients and their roles\n• Micronutrients importance\n• Calorie balance principles\n• How to apply this knowledge with clients",

                        "Macronutrients: The Big Three\n\nMacronutrients provide energy and are needed in large amounts:\n\n🥩 PROTEIN (4 cal/g)\n• Building blocks for muscle\n• Supports immune function\n• Helps with satiety\n• Sources: meat, fish, eggs, legumes\n\n🍞 CARBOHYDRATES (4 cal/g)\n• Primary energy source\n• Fuel for brain and muscles\n• Sources: grains, fruits, vegetables\n\n🥑 FATS (9 cal/g)\n• Hormone production\n• Vitamin absorption\n• Sources: oils, nuts, avocados",

                        "Understanding Calorie Balance\n\nWeight management comes down to energy balance:\n\n📈 SURPLUS: Calories In > Calories Out = Weight Gain\n⚖️ MAINTENANCE: Calories In = Calories Out = Stable Weight\n📉 DEFICIT: Calories In < Calories Out = Weight Loss\n\nFactors affecting 'Calories Out':\n• Basal Metabolic Rate (BMR)\n• Physical activity\n• Thermic effect of food\n• Non-exercise activity thermogenesis",

                        "Micronutrients: Small but Mighty\n\nVitamins and minerals are needed in smaller amounts but are crucial for:\n\n💪 Energy production\n🛡️ Immune function\n🦴 Bone health\n🧠 Brain function\n\nKey principles:\n• Variety ensures adequate intake\n• Whole foods > supplements\n• Some nutrients work together\n• Deficiencies can impact performance",

                        "Applying Nutrition Knowledge\n\nAs a trainer, you should:\n\n✅ Assess current eating patterns\n✅ Educate on portion sizes\n✅ Help set realistic nutrition goals\n✅ Refer to registered dietitians when needed\n\n❌ Don't provide meal plans unless qualified\n❌ Don't diagnose nutritional deficiencies\n❌ Don't recommend extreme diets\n\nRemember: Small, sustainable changes lead to lasting results!"
                };
                break;

            case "BMR & TDEE Calculations":
                tutorialSteps = new String[]{
                        "Understanding Metabolic Calculations\n\nBMR and TDEE are fundamental for setting calorie targets:\n\n🔥 BMR (Basal Metabolic Rate): Energy needed at rest\n⚡ TDEE (Total Daily Energy Expenditure): BMR + activity\n\nWhy this matters:\n• Accurate calorie targets\n• Realistic expectations\n• Better results for clients",

                        "BMR Calculation Methods\n\nHarris-Benedict Equation (Revised):\n\n👨 Men: BMR = 88.362 + (13.397 × weight in kg) + (4.799 × height in cm) - (5.677 × age)\n\n👩 Women: BMR = 447.593 + (9.247 × weight in kg) + (3.098 × height in cm) - (4.330 × age)\n\nExample: 30-year-old woman, 65kg, 165cm\nBMR = 447.593 + (9.247 × 65) + (3.098 × 165) - (4.330 × 30)\nBMR = 447.593 + 601.055 + 511.17 - 129.9 = 1,429.9 calories",

                        "Activity Level Multipliers\n\nTo calculate TDEE, multiply BMR by activity factor:\n\n🛌 Sedentary (desk job, no exercise): BMR × 1.2\n🚶 Lightly Active (light exercise 1-3 days): BMR × 1.375\n🏃 Moderately Active (moderate exercise 3-5 days): BMR × 1.55\n💪 Very Active (hard exercise 6-7 days): BMR × 1.725\n🏋️ Super Active (very hard exercise, physical job): BMR × 1.9\n\nUsing our example:\nModerately active woman: 1,430 × 1.55 = 2,216 calories/day",

                        "Practical Application\n\nUsing TDEE for goal setting:\n\n🎯 Weight Loss: TDEE - 300-500 calories (0.5-1 lb/week)\n⚖️ Maintenance: TDEE ± 100 calories\n📈 Weight Gain: TDEE + 300-500 calories\n\nImportant considerations:\n• These are estimates - adjust based on results\n• Track for 2-3 weeks before making changes\n• Account for metabolic adaptation\n• Individual variations are normal"
                };
                break;

            default:
                tutorialSteps = new String[]{
                        "Tutorial content is being prepared...",
                        "This tutorial will be available soon!",
                        "Thank you for your patience."
                };
        }
    }

    private void displayCurrentStep() {
        if (currentStep < tutorialSteps.length) {
            stepNumberText.setText("Step " + (currentStep + 1) + " of " + tutorialSteps.length);
            stepContentText.setText(tutorialSteps[currentStep]);

            // Update progress bar
            int progress = ((currentStep + 1) * 100) / tutorialSteps.length;
            tutorialProgressBar.setProgress(progress);

            // Update button visibility
            previousButton.setVisibility(currentStep > 0 ? View.VISIBLE : View.INVISIBLE);

            if (currentStep == tutorialSteps.length - 1) {
                nextButton.setVisibility(View.GONE);
                completeButton.setVisibility(View.VISIBLE);
            } else {
                nextButton.setVisibility(View.VISIBLE);
                completeButton.setVisibility(View.GONE);
            }
        }
    }

    private void setupButtons() {
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStep > 0) {
                    currentStep--;
                    displayCurrentStep();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStep < tutorialSteps.length - 1) {
                    currentStep++;
                    displayCurrentStep();
                }
            }
        });

        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TutorialContentActivity.this,
                        "Tutorial completed! +25 points earned",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}