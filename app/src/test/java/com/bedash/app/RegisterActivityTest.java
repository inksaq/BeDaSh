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
public class RegisterActivityTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityScenarioRule =
            new ActivityScenarioRule<>(RegisterActivity.class);

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
    public void registerWithValidInput_createsAccount() {
        // Mock successful registration
        when(mockAuthTask.isSuccessful()).thenReturn(true);
        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockAuthTask);
            return null;
        }).when(mockAuthTask).addOnCompleteListener(any());

        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        // Access the activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.mAuth = mockAuth;
        });

        // Input valid registration data
        onView(withId(R.id.email_edit_text)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password_edit_text)).perform(typeText("password123"), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.register_button)).perform(click());

        // Verify success toast
        onView(withText("Account created successfully!"))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }

    @Test
    public void registerWithShortPassword_showsError() {
        // Input valid email but short password
        onView(withId(R.id.email_edit_text)).perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password_edit_text)).perform(typeText("pass"), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.register_button)).perform(click());

        // Verify error toast
        onView(withText("Password should be at least 6 characters"))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }

    @Test
    public void registerWithEmptyFields_showsError() {
        // Leave fields empty
        // Click register button
        onView(withId(R.id.register_button)).perform(click());

        // Verify error toast
        onView(withText("Please fill in all fields"))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }

    @Test
    public void registrationFailure_showsErrorMessage() {
        // Mock failed registration
        when(mockAuthTask.isSuccessful()).thenReturn(false);
        when(mockAuthTask.getException()).thenReturn(new Exception("Email already in use"));

        doAnswer(invocation -> {
            OnCompleteListener<AuthResult> listener = invocation.getArgument(0);
            listener.onComplete(mockAuthTask);
            return null;
        }).when(mockAuthTask).addOnCompleteListener(any());

        when(mockAuth.createUserWithEmailAndPassword(anyString(), anyString()))
                .thenReturn(mockAuthTask);

        // Access the activity
        activityScenarioRule.getScenario().onActivity(activity -> {
            activity.mAuth = mockAuth;
        });

        // Input registration data
        onView(withId(R.id.email_edit_text)).perform(typeText("existing@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password_edit_text)).perform(typeText("password123"), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.register_button)).perform(click());

        // Verify error toast
        onView(withText("Registration failed: Email already in use"))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }
}