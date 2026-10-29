package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.ArtemisImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.impl.BaseImageProviderImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.view_result.ViewQuizResultScreen
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Practising a quiz that has ended, through the screens of the app: answering, submitting, and the result the
 * student is taken to, which is not saved anywhere but held by the app.
 */
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
// The drag and drop question shows its background image, which needs the native graphics of Robolectric to be decoded
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h1200dp")
internal class QuizPracticeUiE2eTest : QuizBaseE2eTest(QuizType.Practice) {

    private var quizId: Long = 0L

    override suspend fun setupHook() {
        super.setupHook()

        val quiz = createQuiz(getAdminAccessToken(), courseId, randomizeQuestionOrder = false)
        quizId = quiz.id
        endQuizExerciseNow(getAdminAccessToken(), quizId)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `practising shows the points of the answers on the result screen`() {
        val imageProvider = ArtemisImageProviderImpl(get<ArtemisContextProvider>(), BaseImageProviderImpl())
        var resultToShow: QuizType.ViewableQuizType? by mutableStateOf(null)

        // Like the route of the app, which swaps the participation for the result screen
        setupUi(quizId) { viewModel ->
            CompositionLocalProvider(LocalArtemisImageProvider provides imageProvider) {
                val result = resultToShow
                if (result == null) {
                    QuizParticipationScreen(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel,
                        onNavigateToInspectResult = { resultToShow = it },
                        onNavigateUp = {}
                    )
                } else {
                    ViewQuizResultScreen(Modifier.fillMaxSize(), quizId, result)
                }
            }
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Practice"), DefaultTimeoutMillis)

        composeTestRule.onNodeWithText("Enter a correct answer option here").performClick()
        composeTestRule.onNodeWithText("SA").performClick()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("is")

        composeTestRule.onNodeWithText("Submit").performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_submit_dialog_positive)))
            .performClick()

        composeTestRule.waitUntilAtLeastOneExists(hasText("You completed the quiz", substring = true), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText("You completed the quiz with 1.5 out of 3 points (50%).").assertExists()
        composeTestRule.onNodeWithText("Your score: 1/1").assertExists()
        composeTestRule.onNodeWithText("Your score: 0.5/1").assertExists()
        composeTestRule.onNodeWithText("Your score: 0/1").assertExists()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `a practice says that it is one and does not save on the way`() {
        setupUi(quizId) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }

        composeTestRule.waitUntilAtLeastOneExists(hasText("Practice"), DefaultTimeoutMillis)
        composeTestRule.onNodeWithText("Enter a correct answer option here").performClick()
        composeTestRule.waitForIdle()

        // A live quiz says when it saved last; a practice says that it is one
        composeTestRule.onAllNodesWithText("Saved:", substring = true).assertCountEquals(0)
    }
}
