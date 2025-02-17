package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.ktorProvider
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizPracticeParticipationE2eTest : QuizParticipationBaseE2eTest(QuizType.Practice) {

    override suspend fun setupHook() {
        super.setupHook()

        ktorProvider.ktorClient.put(testServerUrl) {
            url {
                appendPathSegments("api", "quiz-exercises", quiz.id.toString(), "end-now")
            }

            cookieAuth(getAdminAccessToken())
            contentType(ContentType.Application.Json)
        }

        ktorProvider.ktorClient.put(testServerUrl) {
            url {
                appendPathSegments("api", "quiz-exercises", quiz.id.toString(), "open-for-practice")
            }

            cookieAuth(getAdminAccessToken())
            contentType(ContentType.Application.Json)
        }

        quizExerciseService
            .join(quiz.id, "", testServerUrl, accessToken)
            .orThrow("Could not start quiz participation")
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - empty submission`() {
        testQuizSubmissionImpl(
            setupAndVerify = { _, submit -> submit() }
        )
    }

    @Ignore("This test was super flaky, so we are ignoring it for now")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - multiple choice`() {
        testSubmitMultipleChoiceImpl()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - short answer`() {
        testSubmitShortAnswerImpl()
    }

    @Ignore("This test was super flaky, so we are ignoring it for now")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can submit practice quiz - drag and drop`() {
        testSubmitDragAndDropImpl()
    }
}