package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.quiz.QuizQuestion
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.Attachment
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("quiz")
data class QuizExercise(
    override val id: Int? = null,
    override val title: String? = null,
    override val shortName: String? = null,
    override val maxPoints: Float? = null,
    override val bonusPoints: Float? = null,
    override val releaseDate: Instant? = null,
    override val dueDate: Instant? = null,
    override val assessmentDueDate: Instant? = null,
    override val difficulty: Difficulty? = null,
    override val mode: Mode = Mode.INDIVIDUAL,
    override val categories: List<Category> = emptyList(),
    override val visibleToStudents: Boolean? = null,
    override val teamMode: Boolean? = null,
    override val problemStatement: String? = null,
    override val assessmentType: AssessmentType? = null,
    override val allowComplaintsForAutomaticAssessments: Boolean? = null,
    override val allowManualFeedbackRequests: Boolean? = null,
    override val includedInOverallScore: IncludedInOverallScore = IncludedInOverallScore.INCLUDED_COMPLETELY,
    override val exampleSolutionPublicationDate: Instant? = null,
    override val attachments: List<Attachment> = emptyList(),
    override val studentParticipations: List<Participation>? = null,
    val allowedNumberOfAttempts: Int? = null,
    val remainingNumberOfAttempts: Int? = null,
    val randomizeQuestionOrder: Boolean? = null,
    val isOpenForPractice: Boolean? = null,
    val duration: Int? = null,
    val quizQuestions: List<QuizQuestion>? = null,
    val status: QuizStatus? = null,
    val quizMode: QuizMode = QuizMode.INDIVIDUAL,
    val quizBatches: List<QuizBatch>? = null
) : Exercise() {

    override fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise = copy(studentParticipations = newParticipations)

    enum class QuizStatus {
        CLOSED,
        OPEN_FOR_PRACTICE,
        ACTIVE,
        VISIBLE,
        INVISIBLE,
    }

    enum class QuizMode {
        SYNCHRONIZED,
        BATCHED,
        INDIVIDUAL,
    }

    @Serializable
    data class QuizBatch(
        val id: Int? = null,
        val startTime: Instant? = null,
        val started: Boolean? = null,
        val ended: Boolean? = null,
        val submissionAllowed: Boolean? = null,
        val password: String? = null
    )
}