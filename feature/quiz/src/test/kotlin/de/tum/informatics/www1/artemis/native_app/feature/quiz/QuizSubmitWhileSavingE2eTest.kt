package de.tum.informatics.www1.artemis.native_app.feature.quiz

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createQuiz
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.quiz.participation.QuizParticipationScreen
import de.tum.informatics.www1.artemis.native_app.feature.quiz.screens.work.TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.QuizParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.quiz.service.impl.QuizParticipationServiceImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * A student who changes an answer and submits right after: the app saves an answer on its own, and that upload is
 * still on its way when the student submits.
 *
 * Artemis creates the answers twice when a save and a submit reach it at the same time, and scores the one it
 * happens to find, which was the old answer in about half of the cases. So the app must not send them at the same
 * time. The save takes long to be done here, so that the student certainly submits while it is on its way.
 */
@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizSubmitWhileSavingE2eTest : QuizBaseE2eTest(QuizType.Live) {

    private lateinit var quiz: QuizExercise

    private val wrongOptionText = "Enter a wrong answer option here"
    private val correctOptionText = "Enter a correct answer option here"

    private val savesInFlight = AtomicInteger()
    private val submitsInFlight = AtomicInteger()

    /** Uploads that were sent while another kind of upload was still on its way */
    private val overlaps = CopyOnWriteArrayList<String>()

    override fun overrideModules(): List<Module> = listOf(
        module {
            single<QuizParticipationService> {
                val real = QuizParticipationServiceImpl(get<KtorProvider>())

                object : QuizParticipationService by real {
                    override suspend fun saveForLiveMode(
                        submission: QuizSubmission,
                        exerciseId: Long,
                        serverUrl: String,
                        authToken: String
                    ): NetworkResponse<Submission> {
                        if (submitsInFlight.get() > 0) overlaps += "a save was sent while the submit was on its way"
                        savesInFlight.incrementAndGet()
                        try {
                            val response = real.saveForLiveMode(submission, exerciseId, serverUrl, authToken)
                            // The save takes long to be done, as on a slow connection
                            withContext(Dispatchers.IO) { Thread.sleep(1_500) }
                            return response
                        } finally {
                            savesInFlight.decrementAndGet()
                        }
                    }

                    override suspend fun submitForLiveMode(
                        submission: QuizSubmission,
                        exerciseId: Long,
                        serverUrl: String,
                        authToken: String
                    ): NetworkResponse<Submission> {
                        if (savesInFlight.get() > 0) overlaps += "the submit was sent while a save was on its way"
                        submitsInFlight.incrementAndGet()
                        try {
                            return real.submitForLiveMode(submission, exerciseId, serverUrl, authToken)
                        } finally {
                            submitsInFlight.decrementAndGet()
                        }
                    }
                }
            }
        }
    )

    override suspend fun setupHook() {
        super.setupHook()

        quiz = createQuiz(getAdminAccessToken(), courseId, randomizeQuestionOrder = false)
        participationService.findParticipation(quiz.id).orThrow("Could not start quiz participation")
        quizExerciseService.join(quiz.id, "", testServerUrl, accessToken).orThrow("Could not join the quiz")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `submitting while an answer is still being saved keeps only the answer that was submitted`() {
        setupUi(quiz.id) { viewModel ->
            QuizParticipationScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateToInspectResult = {},
                onNavigateUp = {}
            )
        }
        composeTestRule.waitUntilExactlyOneExists(hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN), DefaultTimeoutMillis)

        // The wrong option is saved on its own, and the upload takes 2 seconds. Meanwhile the student changes their mind
        composeTestRule.onNodeWithText(wrongOptionText).performClick()
        composeTestRule.onNodeWithText(wrongOptionText).performClick()
        composeTestRule.onNodeWithText(correctOptionText).performClick()

        composeTestRule.onNodeWithText(context.getString(R.string.quiz_participation_submit_button)).performClick()
        composeTestRule
            .onNode(hasAnyAncestor(isDialog()) and hasText(context.getString(R.string.quiz_participation_submit_dialog_positive)))
            .performClick()
        composeTestRule.waitUntil(DefaultTimeoutMillis) {
            composeTestRule.waitForIdle()
            composeTestRule.onAllNodes(hasTestTag(TEST_TAG_WORK_ON_QUIZ_QUESTIONS_SCREEN)).fetchSemanticsNodes().isEmpty()
        }

        // Whatever is still on its way has arrived after this
        Thread.sleep(4_000)
        composeTestRule.waitForIdle()

        assertEquals(emptyList(), overlaps.toList(), "Artemis creates the answers twice when a save and a submit reach it together")

        val submission = runBlockingWithTestTimeout {
            assertNotNull(participationService.findParticipation(quiz.id).orThrow("Could not load the participation").quizSubmission)
        }
        assertEquals(true, submission.submitted, "The quiz should stay submitted")
        val answers = submission.submittedAnswers.filterIsInstance<MultipleChoiceSubmittedAnswer>()
        assertEquals(1, answers.size, "There should be one answer to the question, not one for every upload")
        assertEquals(listOf(correctOptionText), answers.single().selectedOptions.map { it.text })

        // And it is the answer that counts
        runBlockingWithTestTimeout { endQuizExerciseNow(getAdminAccessToken(), quiz.id) }
        val scored = runBlockingWithTestTimeout {
            assertNotNull(participationService.findParticipation(quiz.id).orThrow("Could not load the participation").quizSubmission)
        }
        assertEquals(1.0, scored.scoreInPoints, "The right answer was submitted last, so it must be the one that is scored")
    }
}
