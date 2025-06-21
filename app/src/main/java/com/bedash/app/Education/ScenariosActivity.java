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

public class ScenariosActivity extends BaseActivity {
    private ListView scenarioListView;
    private ArrayList<Map<String, String>> scenarioList;
    private SimpleAdapter adapter;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scenarios);
        setupBase();
        initializeViews();
        setupScenarioList();
        setupButton();
    }

    private void initializeViews() {
        scenarioListView = findViewById(R.id.scenario_list_view);
        backButton = findViewById(R.id.back_button);
    }

    private void setupButton(){
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupScenarioList() {
        scenarioList = new ArrayList<>();

        // Add practice scenarios
        addScenario("The Plateau Client",
                "Sarah has been following her program for 3 months but hasn't seen progress in 4 weeks. How do you help her break through?",
                "Intermediate", "10-15 min");

        addScenario("The Injured Athlete",
                "A competitive runner comes to you with a knee injury. How do you modify their training and nutrition?",
                "Advanced", "15-20 min");

        addScenario("The Busy Executive",
                "A CEO has only 30 minutes, 3 times per week to exercise and frequently travels. Design their program.",
                "Intermediate", "10-15 min");

        addScenario("The Teenage Athlete",
                "A 16-year-old wants to build muscle for football season. What special considerations apply?",
                "Advanced", "12-18 min");

        addScenario("The Post-Pregnancy Client",
                "A new mother wants to return to fitness 6 months after giving birth. What's your approach?",
                "Intermediate", "15-20 min");

        addScenario("The Vegan Bodybuilder",
                "A client wants to build significant muscle mass while maintaining a strict vegan diet. Plan their nutrition.",
                "Advanced", "20-25 min");

        addScenario("The Senior Beginner",
                "A 65-year-old who has never exercised wants to start a fitness routine. What do you recommend?",
                "Beginner", "12-15 min");

        addScenario("The Yo-Yo Dieter",
                "A client has tried every diet and always regains weight. How do you break this cycle?",
                "Intermediate", "15-20 min");

        // Set up adapter
        adapter = new SimpleAdapter(
                this,
                scenarioList,
                R.layout.scenario_list_item,
                new String[]{"title", "description", "level", "duration"},
                new int[]{R.id.scenario_title, R.id.scenario_description, R.id.scenario_level, R.id.scenario_duration}
        );

        scenarioListView.setAdapter(adapter);

        // Set item click listener
        scenarioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> selectedScenario = scenarioList.get(position);
                openScenario(selectedScenario);
            }
        });
    }

    private void addScenario(String title, String description, String level, String duration) {
        Map<String, String> scenario = new HashMap<>();
        scenario.put("title", title);
        scenario.put("description", description);
        scenario.put("level", level);
        scenario.put("duration", duration);
        scenarioList.add(scenario);
    }

    private void openScenario(Map<String, String> scenario) {
        Intent intent = new Intent(ScenariosActivity.this, ScenarioDetailActivity.class);
        intent.putExtra("scenario_title", scenario.get("title"));
        intent.putExtra("scenario_description", scenario.get("description"));
        intent.putExtra("scenario_level", scenario.get("level"));
        intent.putExtra("scenario_duration", scenario.get("duration"));
        startActivity(intent);
    }
}