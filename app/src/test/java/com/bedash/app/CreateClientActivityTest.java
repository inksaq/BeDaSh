package com.bedash.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bedash.app.TestUtils.hasErrorText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateClientActivityTest {

    @Rule
    public ActivityScenarioRule<CreateClientActivity> activityScenarioRule =
            new ActivityScenarioRule<>(CreateClientActivity.class);

    @Test
    public void fillClientForm_proceedsToNextScreen() {
        // Fill in client details
        onView(withId(R.id.et_client_name)).perform(typeText("Test Client"), closeSoftKeyboard());
        onView(withId(R.id.et_client_age)).perform(typeText("35"), closeSoftKeyboard());
        onView(withId(R.id.et_client_weight)).perform(typeText("75.5"), closeSoftKeyboard());
        onView(withId(R.id.et_client_height)).perform(typeText("178"), closeSoftKeyboard());

        // Select gender (first option)
        onView(withId(R.id.spinner_gender)).perform(click());
        onView(withText("Male")).perform(click());

        // Select activity level (first option)
        onView(withId(R.id.spinner_activity)).perform(click());
        onView(withText("Sedentary")).perform(click());

        // Click next button
        onView(withId(R.id.btn_next)).perform(click());

        // Note: For a complete test we would need Intents testing
        // to verify the correct activity was launched
    }

    @Test
    public void incompleteForm_showsValidationErrors() {
        // Leave fields empty
        // Click next button
        onView(withId(R.id.btn_next)).perform(click());

        // Check for validation errors
        onView(withId(R.id.et_client_name)).check(matches(hasErrorText("Name is required")));
    }

    @Test
    public void backButtonNavigation_returnsToCorrectScreen() {
        // Click back button
        onView(withId(R.id.btn_back)).perform(click());

        // Note: We would need Intents testing to verify
        // the correct activity was navigated to
    }
}