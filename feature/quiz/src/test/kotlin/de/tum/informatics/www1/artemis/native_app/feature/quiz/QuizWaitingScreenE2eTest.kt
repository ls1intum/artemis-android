package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.ApiEndpoint
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.addQuizExerciseBatch
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createExerciseFormBodyWithPng
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuizExercise
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.startQuizExerciseBatch
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationUi
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.TEST_TAG_TEXT_FIELD_BATCH_PASSWORD
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.TEST_TAG_WAIT_FOR_QUIZ_START_SCREEN
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizWaitingScreenE2eTest : QuizBaseE2eTest(QuizType.Live) {

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("Quiz participation currently fails to load. See https://github.com/ls1intum/artemis-android/issues/107")
    fun `can start individual quiz`() {
        val quiz: QuizExercise = runBlockingWithTestTimeout {
            val path = getBackgroundImageFilePath()
            assertIs(
                createExerciseFormBodyWithPng(
                    getAdminAccessToken(),
                    courseId,
                    pathSegments = ApiEndpoint.quiz_quizExercises,
                    pngFilePath = path,
                    pngByteArray = getBackgroundImageBytes(),
                    creator = { name, courseId ->
                        createQuizExercise(name, courseId, path, QuizExercise.QuizMode.INDIVIDUAL)
                    }
                )
            )
        }

        setupUi(quiz.id) { viewModel ->
            QuizParticipationUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

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

        val participation = runBlockingWithTestTimeout {
            participationService
                .findParticipation(quiz.id)
                .orThrow("Could not load participation. Expected a participation to have been created")
        }

        assertIs<StudentParticipation>(participation)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("Quiz participation currently fails to load. See https://github.com/ls1intum/artemis-android/issues/107")
    fun `can start batched quiz`() {
        val (quiz, batch) = runBlockingWithTestTimeout {
            val path = getBackgroundImageFilePath()

            val quiz: QuizExercise = assertIs(
                createExerciseFormBodyWithPng(
                    getAdminAccessToken(),
                    courseId,
                    pathSegments = ApiEndpoint.quiz_quizExercises,
                    pngByteArray = getBackgroundImageBytes(),
                    pngFilePath = path,
                    creator = { name, courseId ->
                        createQuizExercise(name, courseId, path, QuizExercise.QuizMode.BATCHED)
                    }
                )
            )

            val batch = addQuizExerciseBatch(getAdminAccessToken(), quiz.id)

            quiz to batch

        }

        setupUi(quiz.id) { viewModel ->
            QuizParticipationUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }


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
        runBlockingWithTestTimeout {
            startQuizExerciseBatch(getAdminAccessToken(), quiz.id, batch)
        }

        val participation = runBlockingWithTestTimeout {
            participationService
                .findParticipation(quiz.id)
                .orThrow("Could not load participation. Expected a participation to have been created")
        }

        assertIs<StudentParticipation>(participation)
    }
}
