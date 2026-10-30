package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.viewresult

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult.*
import kotlin.time.Instant
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category

@Category(UnitTest::class)
class ResultAssessmentStateTest {
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    @Test fun `automatic result remains preliminary before manual assessment even without recursive links`() {
        val exercise = ProgrammingExercise(assessmentType = Exercise.AssessmentType.SEMI_AUTOMATIC)
        val result = Result(assessmentType = Exercise.AssessmentType.AUTOMATIC, completionDate = now)
        assertEquals(ReviewAssessmentState.PRELIMINARY, reviewAssessmentState(exercise, result, false, now))
        assertEquals(ReviewAssessmentState.FINAL, reviewAssessmentState(exercise, result.copy(assessmentType = Exercise.AssessmentType.MANUAL), false, now))
        assertEquals(ReviewAssessmentState.FINAL, reviewAssessmentState(exercise, result, true, now))
    }
    @Test fun `delayed tests and missing completion dates retain preliminary status`() {
        val exercise = ProgrammingExercise(assessmentType = Exercise.AssessmentType.AUTOMATIC, buildAndTestStudentSubmissionsAfterDueDate = Instant.parse("2026-10-27T12:00:00Z"))
        assertEquals(ReviewAssessmentState.PRELIMINARY, reviewAssessmentState(exercise, Result(completionDate = now), false, now))
        assertEquals(ReviewAssessmentState.PRELIMINARY, reviewAssessmentState(exercise, Result(), false, now))
    }
    @Test fun `AI failure pending and successful feedback remain distinct`() {
        val json = JsonProvider().applicationJsonConfiguration
        val pending = json.decodeFromString<Result>("""{"assessmentType":"AUTOMATIC_ATHENA","completionDate":"2026-10-27T12:00:00Z"}""")
        val exercise = ProgrammingExercise()
        assertEquals(ReviewAssessmentState.AI_PENDING, reviewAssessmentState(exercise, pending, false, now))
        assertEquals(ReviewAssessmentState.AI_TIMED_OUT, reviewAssessmentState(exercise, pending.copy(completionDate = Instant.parse("2026-10-25T12:00:00Z")), false, now))
        assertEquals(ReviewAssessmentState.AI_FAILED, reviewAssessmentState(exercise, pending.copy(successful = false), false, now))
        assertEquals(ReviewAssessmentState.PRELIMINARY, reviewAssessmentState(exercise, pending.copy(successful = true), false, now))
    }
    @Test fun `manual code feedback resolves zero based ranges and rejects malformed locations`() {
        assertEquals(ReviewCodeLocation("src/Search.java", 0, 2), ReviewFeedback(reference = "file:src/Search.java_line:0-2").codeLocation())
        assertNull(ReviewFeedback(reference = "file:src/Search.java_line:9-2").codeLocation())
        assertNull(ReviewFeedback(reference = "file:src/Search.java_line:999999999999999999999").codeLocation())
    }
}
