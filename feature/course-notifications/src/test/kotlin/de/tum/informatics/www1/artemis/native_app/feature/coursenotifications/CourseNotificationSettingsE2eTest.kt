package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationSettingsViewModel
import org.junit.Test
import org.junit.experimental.categories.Category
import org.koin.test.get

@Category(EndToEndTest::class)
class CourseNotificationSettingsScreenE2eTest : BaseCourseNotificationTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays notification settings screen with toggle switches and preset dropdown`() {
        setupUi()

        composeTestRule.onNodeWithTag("PresetDropdownTextField").assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can change notification toggle`() {
        setupUi()

        val tag = "NEW_EXERCISE_NOTIFICATION"

        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule
            .onNodeWithTag(tag)
            .performScrollTo()
            .assertExists()
            .performClick()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can open and select preset from dropdown`() {
        setupUi()

        composeTestRule
            .onNodeWithTag("PresetDropdownTextField")
            .assertExists()
            .performClick()

        composeTestRule
            .onNodeWithTag("CUSTOM")
            .assertExists()
            .performClick()
    }

    private fun setupUi() {
        val viewModel = CourseNotificationSettingsViewModel(
            courseId = course.id!!,
            courseNotificationSettingsService = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            CourseNotificationSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }
    }
}
