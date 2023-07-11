package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.participate.text_exercise

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.home.ExerciseScreen
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreUiR

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ExerciseOverviewE2eTest : BaseExerciseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `displays correct exercise data`() {
        setupUiAndViewModel()

        composeTestRole.onAllNodesWithText(exercise.title!!).onFirst().assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can start text exercise`() {
        var participationId: Long? = null

        setupUiAndViewModel { participationId = it }

        composeTestRole.onNodeWithText(
            context.getString(CoreUiR.string.exercise_actions_start_exercise_button)
        )
            .performClick()

        composeTestRole.waitUntil(DefaultTimeoutMillis) { participationId != null }

        composeTestRole
            .waitUntilExactlyOneExists(
                hasText(context.getString(CoreUiR.string.exercise_actions_open_exercise_button)),
                DefaultTimeoutMillis
            )
    }

    @OptIn(KoinInternalApi::class)
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
            coroutineContext = testDispatcher
        )

        composeTestRole.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
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
        }

        return viewModel
    }
}