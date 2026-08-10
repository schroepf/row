package de.mistatee.erglog.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [de.mistatee.erglog.ui.main.MainScreen]. */
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent { MainScreen(username = FAKE_USERNAME) }
    }

    @Test
    fun username_exists() {
        composeTestRule.onNodeWithText("Hello $FAKE_USERNAME!").assertExists()
    }
}

private const val FAKE_USERNAME = "Sample1"
