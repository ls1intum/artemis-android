package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationSettingsViewModel
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class CourseNotificationSettingsScreenE2eTest : BaseCourseNotificationTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays notification settings screen with toggle switches and preset dropdown`() {
        setupUi()

        composeTestRule.onNodeWithText(context.getString(R.string.notification_settings)).assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.setting_disclaimer)).assertExists()

        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.onAllNodes(hasText("Push")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can change notification toggle`() {
        setupUi()

        val toggleText = "Announcements"
        val toggleNode = composeTestRule.onNodeWithText(toggleText)

        toggleNode.assertExists()
        toggleNode.performClick()

    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can open and select preset from dropdown`() {
        setupUi()

        composeTestRule
            .onNode(hasText("Default") or hasText("Custom"))
            .performClick()

        composeTestRule
            .onNodeWithText("Custom")
            .performClick()
    }

    private fun setupUi() {
        val viewModel = CourseNotificationSettingsViewModel(
            courseId = course.id!!,
            courseNotificationSettingsService = get(),
            serverConfigurationService = get(),
            accountService = get(),
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
