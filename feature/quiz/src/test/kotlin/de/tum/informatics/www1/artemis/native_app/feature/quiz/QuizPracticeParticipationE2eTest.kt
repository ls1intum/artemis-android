package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.endQuizExerciseNow
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizPracticeParticipationE2eTest : QuizParticipationBaseE2eTest(QuizType.Practice) {

    override suspend fun setupHook() {
        super.setupHook()

        // Artemis opens a course quiz for practice as soon as it has ended. A practice needs no batch to join.
        endQuizExerciseNow(getAdminAccessToken(), quiz.id)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - empty submission`() {
        testQuizSubmissionImpl(
            setupAndVerify = { _, submit -> submit() }
        )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - multiple choice`() {
        testSubmitMultipleChoiceImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - short answer`() {
        testSubmitShortAnswerImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - drag and drop`() {
        testSubmitDragAndDropImpl()
    }
}