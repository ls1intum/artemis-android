package de.tum.informatics.www1.artemis.native_app.core.model.exercise

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Default deserialized exercise if an exercise is found this app does not know.
 */
@Serializable
data class UnknownExercise(
    override val id: Long = 0L,
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
    override val teamMode: Boolean = false,
    override val studentAssignedTeamId: Long? = null,
    override val studentAssignedTeamIdComputed: Boolean = false,
    override val problemStatement: String? = null,
    override val assessmentType: AssessmentType? = null,
    override val allowComplaintsForAutomaticAssessments: Boolean? = null,
    override val allowManualFeedbackRequests: Boolean? = null,
    override val includedInOverallScore: IncludedInOverallScore = IncludedInOverallScore.INCLUDED_COMPLETELY,
    override val exampleSolutionPublicationDate: Instant? = null,
    override val attachments: List<Attachment> = emptyList(),
    override val studentParticipations: List<Participation>? = null,
    override val course: Course? = null,
    ) : Exercise() {
    override fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise =
        copy(studentParticipations = newParticipations)
}