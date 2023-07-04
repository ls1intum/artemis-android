package de.tum.informatics.www1.artemis.native_app.feature.course_view

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createModelingExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createProgramingExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuizExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createTextExercise
import de.tum.informatics.www1.artemis.native_app.feature.course_view.ui.exercise_list.TEST_TAG_EXERCISE_LIST_LAZY_COLUMN
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ExerciseListOverviewE2eTest : BaseCourseTest() {

    @Test
    fun `display text exercise`() {
        displayExerciseTypeTestImpl("text-exercises", ::createTextExercise)
    }

    @Test
    fun `display modeling exercise`() {
        displayExerciseTypeTestImpl("modeling-exercises", ::createModelingExercise)
    }

    @Test
    fun `display programing exercise`() {
        displayExerciseTypeTestImpl("programming-exercises", ::createProgramingExercise)
    }

    @Test
    fun `display quiz exercise`() {
        displayExerciseTypeTestImpl("quiz-exercises", ::createQuizExercise)
    }

    private fun displayExerciseTypeTestImpl(endpoint: String, creator: (String, Long) -> String) {
        val exercise = runBlocking {
            createExercise(getAdminAccessToken(), course.id!!, endpoint = endpoint, creator = creator)
        }

        setupAndDisplayCourseUi()

        composeTestRule
            .onNodeWithTag(TEST_TAG_EXERCISE_LIST_LAZY_COLUMN)
            .performScrollToKey(exercise.id!!)

        composeTestRule
            .onNodeWithText(exercise.title!!)
            .assertExists("Could not find created exercise. Used endpoint: $endpoint")
    }
}