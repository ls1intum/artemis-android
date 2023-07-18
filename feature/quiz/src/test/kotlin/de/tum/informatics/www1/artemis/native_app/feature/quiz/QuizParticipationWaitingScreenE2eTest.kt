package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.addQuizExerciseBatch
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuizExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.startQuizExerciseBatch
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.TEST_TAG_TEXT_FIELD_BATCH_PASSWORD
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class QuizParticipationWaitingScreenE2eTest : QuizParticipationBaseE2eTest() {

    @Test
    fun `can start individual quiz`() {
        val quiz: QuizExercise = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                assertIs(
                    createExercise(
                        getAdminAccessToken(),
                        courseId,
                        endpoint = "quiz-exercises",
                        creator = { name, courseId ->
                            createQuizExercise(name, courseId, QuizExercise.QuizMode.INDIVIDUAL)
                        }
                    )
                )
            }
        }

        setupUi(quiz.id)

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_start_now))
            .assertExists("start now text missing")

        composeTestRule
            .onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_start_now_button))
            .performClick()

        // Wait until starting is complete. The waiting for start screen will disappear
        composeTestRule
            .waitUntilDoesNotExist(
                hasTestTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN),
                DefaultTimeoutMillis
            )

        val participation = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                participationService
                    .findParticipation(quiz.id, testServerUrl, accessToken)
                    .orThrow("Could not load participation. Expected a participation to have been created")
            }
        }

        assertIs<StudentParticipation>(participation)
    }

    @Test
    fun `can start batched quiz`() {
        val (quiz, batch) = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                val quiz: QuizExercise = assertIs(
                    createExercise(
                        getAdminAccessToken(),
                        courseId,
                        endpoint = "quiz-exercises",
                        creator = { name, courseId ->
                            createQuizExercise(name, courseId, QuizExercise.QuizMode.BATCHED)
                        }
                    )
                )

                val batch = addQuizExerciseBatch(getAdminAccessToken(), quiz.id)

                quiz to batch
            }
        }

        setupUi(quiz.id)

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithTag(TEST_TAG_TEXT_FIELD_BATCH_PASSWORD)
            .performTextInput(assertNotNull(batch.password, "Batch password is null"))

        composeTestRule
            .onNodeWithText(context.getString(R.string.quiz_participation_wait_for_start_join_button))
            .performClick()

        // Wait until starting is complete. The waiting for start screen will disappear
        composeTestRule
            .waitUntilExactlyOneExists(
                hasText(context.getString(R.string.quiz_participation_wait_for_start_explanation)),
                DefaultTimeoutMillis
            )

        // Start the batch to generate a participation
        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                startQuizExerciseBatch(getAdminAccessToken(), quiz.id, batch)
            }
        }

        val participation = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                participationService
                    .findParticipation(quiz.id, testServerUrl, accessToken)
                    .orThrow("Could not load participation. Expected a participation to have been created")
            }
        }

        assertIs<StudentParticipation>(participation)
    }

    @OptIn(KoinInternalApi::class)
    private fun setupUi(exerciseId: Long): QuizParticipationViewModel {
        val viewModel = QuizParticipationViewModel(
            courseId = courseId,
            exerciseId = exerciseId,
            quizType = QuizType.Live,
            savedStateHandle = SavedStateHandle(),
            quizExerciseService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            quizParticipationService = get(),
            websocketProvider = get(),
            networkStatusProvider = get(),
            participationService = get(),
            serverTimeService = get()
        )

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                QuizParticipationUi(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onNavigateToInspectResult = {},
                    onNavigateUp = {}
                )
            }
        }

        return viewModel
    }
}