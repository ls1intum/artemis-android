package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.home.ExerciseScreen
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreUiR

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ExerciseOverviewE2eTest : BaseExerciseTest() {

    @OptIn(ExperimentalTestApi::class)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays correct exercise data`() {
        setupUiAndViewModel()

        // The screen renders once the exercise has been loaded from the server, so nothing it shows
        // is on screen at the moment setContent returns.
        composeTestRule.waitUntilAtLeastOneExists(hasText(exercise.title!!), DefaultTimeoutMillis)

        composeTestRule.onAllNodesWithText(exercise.title!!).onFirst().assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can start text exercise`() {
        var participationId: Long? = null

        setupUiAndViewModel { participationId = it }

        // Same here: the button only exists once the exercise has loaded, and performClick does not
        // wait for it -- it failed on develop with "Failed to inject touch input ... could not find
        // any node that satisfies: Text contains 'Start exercise'".
        composeTestRule.waitUntilExactlyOneExists(
            hasText(context.getString(CoreUiR.string.exercise_actions_start_exercise_button)),
            DefaultTimeoutMillis
        )

        composeTestRule.onNodeWithText(
            context.getString(CoreUiR.string.exercise_actions_start_exercise_button)
        )
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { participationId != null }

        composeTestRule
            .waitUntilExactlyOneExists(
                hasText(context.getString(CoreUiR.string.exercise_actions_open_exercise_button)),
                DefaultTimeoutMillis
            )
    }

    private fun setupUiAndViewModel(
        onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit = {}
    ): ExerciseViewModel {
        val viewModel = ExerciseViewModel(
            exerciseId = exercise.id!!,
            exerciseService = get(),
            channelService = get(),
            liveParticipationService = get(),
            courseExerciseService = get(),
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            ExerciseScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onViewResult = { },
                onViewTextExerciseParticipationScreen = onViewTextExerciseParticipationScreen,
                onParticipateInQuiz = { _, _ -> },
                onClickViewQuizResults = { }
            )
        }

        return viewModel
    }
}