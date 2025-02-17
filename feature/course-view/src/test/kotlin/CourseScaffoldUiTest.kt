import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.feature.courseview.R
import de.tum.informatics.www1.artemis.native_app.feature.courseview.ui.course_overview.CourseScaffold
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@Category(UnitTest::class)
@RunWith(AndroidJUnit4::class)
class CourseScaffoldUiTest : BaseComposeTest() {

    @Test
    fun `test GIVEN a course with faq disable WHEN displaying the bottomNavBar THEN no faq tab is displayed`() {
        val course = Course(
            id = 1,
            faqEnabled = false
        )

        setupUi(course)

        composeTestRule.onNodeWithText(context.getString(R.string.course_ui_tab_faq))
            .assertDoesNotExist()
    }

    @Test
    fun `test GIVEN a course with faq enabled WHEN displaying the bottomNavBar THEN the faq tab is displayed`() {
        val course = Course(
            id = 1,
            faqEnabled = true
        )

        setupUi(course)

        composeTestRule.onNodeWithText(context.getString(R.string.course_ui_tab_faq))
            .assertExists()
    }


    private fun setupUi(course: Course) {
        composeTestRule.setContent {
            CourseScaffold(
                courseDataState = DataState.Success(course),
                isCourseTabSelected = { false },
                updateSelectedCourseTab = {},
                onNavigateBack = {},
                onReloadCourse = {},
                content = {}
            )
        }
    }
}