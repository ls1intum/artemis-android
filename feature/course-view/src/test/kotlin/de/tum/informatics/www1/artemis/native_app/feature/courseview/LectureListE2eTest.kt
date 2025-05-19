package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescriptionExactly
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createLecture
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.TEST_TAG_LECTURE_LIST
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

        // Click on the lectures tab
        composeTestRule
            .onNodeWithText(context.getString(R.string.course_ui_tab_lectures))
            .performClick()

        // Expand all sections by clicking their headers
        composeTestRule
            .onAllNodes(hasTestTagEndingWith("-header")
                .and(hasAnyDescendant(hasContentDescriptionExactly("Expand list"))))
            .fetchSemanticsNodes()
            .forEach { header ->
                composeTestRule
                    .onNode(hasTestTag(header.config.getOrNull(SemanticsProperties.TestTag) ?: ""))
                    .performClick()
            }

        composeTestRule.onNodeWithTag(TEST_TAG_LECTURE_LIST).performScrollToKey(lecture.id!!)
        composeTestRule.onNodeWithText(lecture.title).assertExists()
    }

}
