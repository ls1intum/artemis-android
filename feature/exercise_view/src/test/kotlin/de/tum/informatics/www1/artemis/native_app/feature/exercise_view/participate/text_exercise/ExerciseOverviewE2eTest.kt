package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.ExerciseScreen
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreUiR

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ExerciseOverviewE2eTest : BaseExerciseTest() {

    @Test
    fun `displays correct exercise data`() {
        setupUiAndViewModel()

        composeTestRole.onAllNodesWithText(exercise.title!!).onFirst().assertExists()
    }

    @Test
    fun `can start text exercise`() {
        var participationId: Long? = null

        setupUiAndViewModel { participationId = it }

        composeTestRole.onNodeWithText(
            context.getString(CoreUiR.string.exercise_actions_start_exercise_button)
        )
            .performClick()

        composeTestRole.waitUntil(DefaultTimeoutMillis) { participationId != null }

        composeTestRole
            .onNodeWithText(context.getString(CoreUiR.string.exercise_actions_open_exercise_button))
            .assertExists("After starting the exercise the button should now say \"open exercise\"")
    }

    private fun setupUiAndViewModel(
        onViewTextExerciseParticipationScreen: (participationId: Long) -> Unit = {}
    ): ExerciseViewModel {
        val viewModel = ExerciseViewModel(
            exerciseId = exercise.id!!,
            serverConfigurationService = get(),
            accountService = get(),
            exerciseService = get(),
            liveParticipationService = get(),
            courseExerciseService = get(),
            networkStatusProvider = get(),
            coroutineContext = UnconfinedTestDispatcher()
        )

        composeTestRole.setContent {
            ExerciseScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                navController = rememberNavController(),
                onNavigateBack = { },
                onViewResult = { },
                onViewTextExerciseParticipationScreen = onViewTextExerciseParticipationScreen,
                onParticipateInQuiz = { _, _ -> },
                onClickViewQuizResults = { }
            )
        }

        return viewModel
    }
}