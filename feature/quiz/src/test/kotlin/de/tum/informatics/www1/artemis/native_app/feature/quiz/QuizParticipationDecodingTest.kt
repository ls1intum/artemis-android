package de.tum.informatics.www1.artemis.native_app.feature.quiz

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.MultipleChoiceSubmittedAnswer
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * The payloads in src/test/resources/start-participation are real answers of Artemis 10.1 to
 * `POST quiz-exercises/{id}/start-participation`, which the app sends whenever a live quiz is opened:
 * once while the quiz runs, with a saved answer, and once after it has ended and been evaluated.
 * They are decoded with the configuration of the network layer.
 */
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
internal class QuizParticipationDecodingTest {

    private val json = JsonProvider().applicationJsonConfiguration

    @Test
    fun `decodes the questions of a running quiz with their decimal points`() {
        val participation = decode("live-quiz.json")

        val quiz = assertIs<QuizExercise>(participation.exercise)
        assertEquals(listOf(121L, 122L, 123L), quiz.quizQuestions.map { it.id })
        assertEquals(listOf(1.0, 1.0, 1.0), quiz.quizQuestions.map { it.points })
    }

    @Test
    fun `finds the saved answers of a running quiz in its submission`() {
        val participation = decode("live-quiz.json")

        val submission = assertNotNull(participation.quizSubmission, "No quiz submission found")
        assertEquals(false, submission.submitted)

        val answer = assertIs<MultipleChoiceSubmittedAnswer>(submission.submittedAnswers.single())
        assertEquals(121L, answer.quizQuestion?.id)
        assertEquals(listOf(1L), answer.selectedOptions.map { it.id })

        assertNull(participation.quizResult, "A running quiz has no result yet")
    }

    @Test
    fun `finds the result of an ended quiz on its submission`() {
        val participation = decode("after-quiz-end.json")

        val result = assertNotNull(participation.quizResult, "No quiz result found")
        assertEquals(2L, result.id)
        assertEquals(33.3f, result.score)
        assertEquals(true, result.rated)

        val submission = assertNotNull(participation.quizSubmission, "No quiz submission found")
        assertEquals(true, submission.submitted)
        assertEquals(1.0, submission.scoreInPoints)
    }

    private fun decode(fileName: String): Participation {
        val payload = checkNotNull(javaClass.getResourceAsStream("/start-participation/$fileName")) {
            "$fileName is missing from the test resources"
        }.use { it.reader().readText() }

        return json.decodeFromString<Participation>(payload)
    }
}
