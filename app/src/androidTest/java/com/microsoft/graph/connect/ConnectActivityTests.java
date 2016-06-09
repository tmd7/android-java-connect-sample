/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.graph.connect;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.web.webdriver.DriverAtoms;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.clearElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class ConnectActivityTests {
    private static String testClientId;
    private static String testUsername;
    private static String testPassword;

    @Rule
    public IntentsTestRule<ConnectActivity> mConnectActivityRule = new IntentsTestRule<>(ConnectActivity.class);

    @BeforeClass
    public static void getTestParameters() {
        Bundle extras = InstrumentationRegistry.getArguments();
        testClientId = extras.getString("test_client_id");
        testUsername = extras.getString("test_username");
        testPassword = extras.getString("test_password");
    }

    @Test
    public void displayAzureADSignIn() throws InterruptedException{
        Constants.CLIENT_ID = testClientId;
        onView(withId(R.id.connectButton)).perform(click());

        try {
            onWebView()
                    .withElement(findElement(Locator.ID, "cred_userid_inputtext"))
                    .perform(clearElement())
                    // Enter text into the input element
                    .perform(DriverAtoms.webKeys(testUsername))
                    // Set focus on the username input text
                    // The form validates the username when this field loses focus
                    .perform(webClick())
                    .withElement(findElement(Locator.ID, "cred_password_inputtext"))
                    .perform(clearElement())
                    // Enter text into the input element
                    .perform(DriverAtoms.webKeys(testPassword))
                    // Now we force focus on this element to make
                    // the username element to lose focus and validate
                    .perform(webClick());

            Thread.sleep(2000, 0);

            onWebView()
                    .withElement(findElement(Locator.ID, "cred_sign_in_button"))
                    .perform(webClick());

            Thread.sleep(2000, 0);
        } catch (NoMatchingViewException ex) {
            // If user is already logged in, the flow will go directly to SendMailActivity
        }

        intended(allOf(
                hasComponent(hasShortClassName(".SendMailActivity")),
                hasExtra("displayableId", "ZrinkaM@MOD182601.onmicrosoft.com"),
                toPackage("com.microsoft.graph.connect")
        ));

        onView(withId(R.id.sendMailButton)).perform(click());

        onView(withId(R.id.conclusionTextView))
                .check(matches(withText(R.string.conclusion_text)));

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());

        // Espresso can't find menu items by id. We'll use the text property.
        onView(withText(R.string.disconnect_menu_item_text))
                .perform(click());

        intended(allOf(
                hasComponent(hasShortClassName(".ConnectActivity")),
                toPackage("com.microsoft.graph.connect")
        ));
    }
}
