package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.viewresult

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.TextExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class ResultReviewContractsTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun `public feedback preserves signed long id and standardized message without private attributes`() {
        val feedback = json.decodeFromString<ReviewFeedback>("""{"id":-9223372036854775807,"text":"Title","detailText":"Details","credits":0,"gradingInstruction":{"feedback":"Public message","gradingScale":"PRIVATE","instructionDescription":"SECRET"}}""")
        assertEquals(-9223372036854775807L, feedback.id)
        assertEquals("Public message\nDetails", feedback.message)
        assertEquals("Title", feedback.title)
        assertEquals(0.0, feedback.credits!!, 0.0)
        assertFalse(json.encodeToString(ReviewFeedback.serializer(), feedback).contains("SECRET"))
        assertFalse(json.encodeToString(ReviewFeedback.serializer(), feedback).contains("PRIVATE"))
    }

    @Test fun `text block anchors use raw UTF16 offsets and malformed references remain unlocated`() {
        val text = "A😀e\u0301\r\nrepeat repeat"
        val blockId = "0123456789abcdef0123456789abcdef01234567"
        val source = ReviewSubmission(id = 7, text = text, blocks = listOf(ReviewTextBlock(blockId, 1, 3)))
        assertEquals(1 until 3, source.rangeFor(ReviewFeedback(reference = blockId)))
        assertEquals("😀", text.substring(source.rangeFor(ReviewFeedback(reference = blockId))!!))
        assertEquals(7 until 13, source.rangeFor(ReviewFeedback(reference = "repeat")))
        listOf("99999999999999999999", "absent", "-1", "").forEach { assertNull(source.rangeFor(ReviewFeedback(reference = it))) }
        assertNull(source.copy(blocks = listOf(ReviewTextBlock(blockId, 10, 2))).rangeFor(ReviewFeedback(reference = blockId)))
    }

    @Test fun `explicit result selection never borrows newer submitted work`() {
        val old = TextSubmission(id = 11, text = "assessed", results = listOf(Result(id = 1, rated = true)))
        val newer = TextSubmission(id = 12, text = "new draft", results = listOf(Result(id = 2, rated = true)))
        val participation = StudentParticipation.StudentParticipationImpl(id = 5, submissions = listOf(newer, old))
        val exercise = TextExercise(id = 3, studentParticipations = listOf(participation))
        val selected = selectReviewTarget(exercise, 1)!!
        assertEquals(11L, selected.submissionId)
        assertEquals(5L, selected.participationId)
        assertEquals(1L, selected.result.id)
        assertNull(selectReviewTarget(exercise, 999))
    }

    @Test fun `score formatting distinguishes absent and zero and retains bonus and precision`() {
        assertNull(reviewScore(null, 10f, 1))
        assertEquals("0% · 0 / 10", reviewScore(0f, 10f, 1))
        assertEquals("125% · 12.5 / 10", reviewScore(125f, 10f, 1))
        assertEquals("33.33% · 3.33 / 10", reviewScore(33.333f, 10f, 2))
        assertEquals("40%", reviewScore(40f, null, 1))
    }

    @Test fun `programming grouping retains hidden test names and does not expose internal markers`() {
        val feedback = ReviewFeedback(text = "SCAFeedbackIdentifier:style", type = "AUTOMATIC", positive = false)
        assertEquals(FeedbackGroup.STATIC_ANALYSIS, feedback.group)
        assertEquals("style", feedback.title)
        assertEquals(FeedbackGroup.TESTS, ReviewFeedback(type = "AUTOMATIC", testCase = ReviewTestCase()).group)
        assertEquals(FeedbackGroup.POLICY, ReviewFeedback(text = "SubPolFeedbackIdentifier:penalty", type = "AUTOMATIC").group)
    }
    @Test fun `newer participation level result is considered alongside older nested results`() {
        val old = Result(id = 1, rated = true, completionDate = kotlin.time.Instant.parse("2026-10-20T12:00:00Z"))
        val newer = Result(id = 2, rated = true, completionDate = kotlin.time.Instant.parse("2026-10-21T12:00:00Z"))
        val participation = StudentParticipation.StudentParticipationImpl(id = 5, results = listOf(newer), submissions = listOf(TextSubmission(id = 11, results = listOf(old))))
        assertEquals(2L, de.tum.informatics.www1.artemis.native_app.core.ui.exercise.util.ExerciseParticipationLastRatedUtil.findLatestRatedResult(participation)?.id)
    }

    @Test fun `long feedback preview stays compact without losing the loaded message`() {
        val feedback = ReviewFeedback(detailText = "a".repeat(5000), hasLongFeedbackText = true)
        assertEquals(501, feedback.previewMessage.length)
        assertEquals("a".repeat(500) + "…", feedback.previewMessage)
        assertEquals(5000, feedback.message.length)
        assertEquals("Short note", ReviewFeedback(detailText = "Short note", hasLongFeedbackText = true).previewMessage)
    }

}
