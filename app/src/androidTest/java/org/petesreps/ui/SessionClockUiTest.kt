package org.petesreps.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionClockUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun masterClockShowsTimeAndHardStopRule() {
        composeRule.setContent {
            MaterialTheme {
                SessionClockBanner(90_000L)
            }
        }

        composeRule.onNodeWithTag("session-clock").assertExists()
        composeRule.onNodeWithText("01:30").assertExists()
        composeRule.onNodeWithText("At 00:00, the session stops.").assertExists()
    }

    @Test
    fun expiredMovementTimerSaysMoveOn() {
        composeRule.setContent {
            MaterialTheme {
                BlockClockBanner(0L)
            }
        }

        composeRule.onNodeWithTag("block-clock").assertExists()
        composeRule.onNodeWithText("MOVE ON").assertExists()
    }
}
