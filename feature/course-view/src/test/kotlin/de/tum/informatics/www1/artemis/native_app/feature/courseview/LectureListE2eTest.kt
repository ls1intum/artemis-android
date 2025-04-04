package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createLecture
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.TEST_TAG_LECTURE_LIST
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class LectureListE2eTest : BaseCourseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays created lecture`() {
        val lecture = runBlocking {
            createLecture(getAdminAccessToken(), course.id!!)
        }

        setupAndDisplayCourseUi()

        composeTestRule.onNodeWithText(context.getString(R.string.course_ui_tab_lectures))
            .performClick()

        composeTestRule
            .onNodeWithTag("noDate-header")
            .performClick()

        composeTestRule.onNodeWithTag(TEST_TAG_LECTURE_LIST).performScrollToKey(lecture.id!!)
        composeTestRule.onNodeWithText(lecture.title).assertExists()
    }
}
