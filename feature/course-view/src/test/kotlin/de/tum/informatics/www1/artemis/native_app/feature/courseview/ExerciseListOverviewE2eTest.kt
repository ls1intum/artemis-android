package de.tum.informatics.www1.artemis.native_app.feature.courseview

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createProgramingExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextExercise
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.TEST_TAG_EXERCISE_LIST_LAZY_COLUMN
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ExerciseListOverviewE2eTest : BaseCourseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `display text exercise`() {
        displayExerciseTypeTestImpl {
            createExercise(
                getAdminAccessToken(),
                course.id!!,
                pathSegments = Api.Text.TextExercises.path,
                creator = ::createTextExercise
            )
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `display modeling exercise`() {
        displayExerciseTypeTestImpl {
            createExercise(
                getAdminAccessToken(),
                course.id!!,
                pathSegments = Api.Modeling.ModelingExercises.path,
                creator = ::createModelingExercise
            )
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore
    fun `display programing exercise`() {
        displayExerciseTypeTestImpl {
            createExercise(
                getAdminAccessToken(),
                course.id!!,
                pathSegments = Api.Programming.ProgrammingExercises.path,
                creator = ::createProgramingExercise
            )
        }
    }

    @Ignore("TODO: quiz creation is currently undergoing changes. Fix once those are complete.")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `display quiz exercise`() {
//        displayExerciseTypeTestImpl {
//            createExerciseFormBody(
//                getAdminAccessToken(),
//                course.id!!,
//                endpoint = "quiz-exercises",
//                creator = { ::createQuizExercise }
//            )
//        }
    }


    private fun displayExerciseTypeTestImpl(createExercise: suspend () -> Exercise) {
        val exercise = runBlockingWithTestTimeout {
            createExercise()
        }

        setupAndDisplayCourseUi()

        // Click on the exercises tab
        composeTestRule
            .onNodeWithText(context.getString(R.string.course_ui_tab_exercises))
            .performClick()

        // Expand all sections by clicking their headers
        composeTestRule
            .onAllNodes(hasTestTagEndingWith("-header"))
            .fetchSemanticsNodes()
            .forEach { header ->
                composeTestRule
                    .onNode(hasTestTag(header.config.getOrNull(SemanticsProperties.TestTag) ?: ""))
                    .performClick()
            }

        composeTestRule
            .waitUntilExactlyOneExists(
                hasTestTag(TEST_TAG_EXERCISE_LIST_LAZY_COLUMN),
                DefaultTimeoutMillis
            )

        composeTestRule
            .onNodeWithTag(TEST_TAG_EXERCISE_LIST_LAZY_COLUMN)
            .performScrollToKey(exercise.id!!)

        // Verify the exercise title is visible
        composeTestRule
            .onNodeWithText(exercise.title!!)
            .assertExists("Could not find created exercise.")
    }

}