package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import org.junit.Ignore
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizParticipationE2eTest : QuizParticipationBaseE2eTest(QuizType.Live) {

    override suspend fun setupHook() {
        super.setupHook()

        quizExerciseService
            .join(quiz.id, "", testServerUrl, accessToken)
            .orThrow("Could not start quiz participation")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("Quiz participation currently fails to load. See https://github.com/ls1intum/artemis-android/issues/107")
    fun `can submit live quiz - empty submission`() {
        testQuizSubmissionImpl(
            setupAndVerify = { _, submit -> submit() }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("Quiz participation currently fails to load. See https://github.com/ls1intum/artemis-android/issues/107")
    fun `can submit live quiz - multiple choice`() {
        testSubmitMultipleChoiceImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("Quiz participation currently fails to load. See https://github.com/ls1intum/artemis-android/issues/107")
    fun `can submit live quiz - short answer`() {
        testSubmitShortAnswerImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    @Ignore("Quiz participation currently fails to load. See https://github.com/ls1intum/artemis-android/issues/107")
    fun `can submit live quiz - drag and drop`() {
        testSubmitDragAndDropImpl()
    }
}
