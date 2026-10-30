package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalTestApi::class)
class CourseOverviewE2eTest : BaseCourseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays correct course title`() {
        setupAndDisplayCourseUi()

        composeTestRule.waitUntilExactlyOneExists(hasText(course.title), DefaultTimeoutMillis)
    }
}
