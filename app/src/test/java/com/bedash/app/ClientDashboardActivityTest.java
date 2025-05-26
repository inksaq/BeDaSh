package com.bedash.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bedash.app.TestUtils.isToast;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ClientDashboardActivityTest {

    @Test
    public void displayClientData_showsCorrectInformation() {
        // Create intent with client data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClientDashboardActivity.class);
        intent.putExtra("client_id", "test_client_id");
        intent.putExtra("client_name", "Test Client");

        // Launch with intent
        try (ActivityScenario<ClientDashboardActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify client name is displayed
            onView(withId(R.id.client_name_text)).check(matches(withText("Client: Test Client")));
            onView(withId(R.id.day_text)).check(matches(withText("Day: Today")));
        }
    }

    @Test
    public void clickFoodLogButton_showsToast() {
        // Create intent with client data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClientDashboardActivity.class);
        intent.putExtra("client_id", "test_client_id");
        intent.putExtra("client_name", "Test Client");

        // Launch with intent
        try (ActivityScenario<ClientDashboardActivity> scenario = ActivityScenario.launch(intent)) {
            // Click food log button
            onView(withId(R.id.food_log_button)).perform(click());

            // Verify toast appears
            onView(withText("Food log feature coming soon"))
                    .inRoot(isToast())
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void missingClientData_finishesActivity() {
        // Create intent without client data
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ClientDashboardActivity.class);

        // Launch with intent
        try (ActivityScenario<ClientDashboardActivity> scenario = ActivityScenario.launch(intent)) {
            // Activity should finish itself due to missing data
            scenario.onActivity(activity -> {
                assert(activity.isFinishing());
            });
        }
    }
}
