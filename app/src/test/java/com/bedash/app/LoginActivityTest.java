package com.bedash.app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bedash.app.TestUtils.isToast;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private Task<AuthResult> mockAuthTask;

    @Mock
    private FirebaseUser mockUser;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set up mocks for Firebase Auth
        when(mockAuth.getCurrentUser()).thenReturn(null);
    }

    @Test
    public void loginWithValidCredentials_navigatesToDashboard() {
        // Mock successful login
        when(mockAuthTask.isSuccessful()).thenReturn(true);
        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockAuthTask);
            return null;
        }).when(mockAuthTask).addOnCompleteListener(any());

        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        // Access the activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.mAuth = mockAuth;
        });

        // Input valid credentials
        onView(withId(R.id.email_edit_text)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password_edit_text)).perform(typeText("password123"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.login_button)).perform(click());

        // Note: For a complete test we would need to set up Intents testing
        // Using Espresso-Intents library to verify the correct Intent was launched
    }

    @Test
    public void loginWithInvalidCredentials_showsErrorMessage() {
        // Mock failed login
        when(mockAuthTask.isSuccessful()).thenReturn(false);
        when(mockAuthTask.getException()).thenReturn(new Exception("Authentication failed"));

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockAuthTask);
            return null;
        }).when(mockAuthTask).addOnCompleteListener(any());

        when(mockAuth.signInWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        // Access the activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.mAuth = mockAuth;
        });

        // Input invalid credentials
        onView(withId(R.id.email_edit_text)).perform(typeText("wrong@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password_edit_text)).perform(typeText("wrongpassword"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.login_button)).perform(click());

        // Verify error toast is shown
        onView(withText("Authentication failed: Authentication failed"))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }

    @Test
    public void emptyFields_showsToastMessage() {
        // Leave fields empty
        // Click login button
        onView(withId(R.id.login_button)).perform(click());

        // Verify toast message
        onView(withText("Please enter username and password"))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }
}