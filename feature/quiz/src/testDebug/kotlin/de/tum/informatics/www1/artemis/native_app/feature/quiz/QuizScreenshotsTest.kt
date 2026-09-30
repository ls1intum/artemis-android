package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The screenshot for the Play Store is a preview of the debug variant that nothing else builds or shows, and
 * it was commented out for years because it no longer compiled. This renders it, so that it keeps working.
 */
@OptIn(ExperimentalTestApi::class)
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class QuizScreenshotsTest : BaseComposeTest() {

    @Test
    fun `the screenshot of a multiple choice question shows the question and its answers`() {
        composeTestRule.setContent { `Quiz - Multiple Choice Question`() }

        composeTestRule.waitUntilAtLeastOneExists(hasText("1) Solid chemical propellants"), 10_000)

        composeTestRule.onNodeWithText("Participate in your course quizzes").assertExists()
        composeTestRule.onNodeWithText("What is the primary advantage of using solid chemical propellants in rocket engines?").assertExists()
        composeTestRule.onNodeWithText("High thrust-to-weight ratio 📈").assertExists()
        composeTestRule.onNodeWithText("Easy in-flight thrust adjustments 🧭").assertExists()
        composeTestRule.onNodeWithText("Lower cost 💰").assertExists()
        composeTestRule.onNodeWithText("Overall: 7P").assertExists()
        composeTestRule.onNodeWithText("Quiz: Rocket fuels ⛽").assertExists()
    }
}
