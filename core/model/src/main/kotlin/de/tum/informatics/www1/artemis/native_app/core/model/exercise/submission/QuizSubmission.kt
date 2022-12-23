package de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.quiz.SubmittedAnswer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("quiz")
data class QuizSubmission(
    override val id: Int? = null,
    override val submitted: Boolean? = null,
    override val submissionDate: Instant? = null,
    override val exampleSubmission: Boolean? = null,
    override val durationInMinutes: Float? = null,
    override val results: List<Result>? = null,
    override val participation: Participation? = null,
    override val submissionType: SubmissionType? = null,
    val scoreInPoints: Double? = null,
    val submittedAnswers: List<SubmittedAnswer> = emptyList()
) : Submission() {

}