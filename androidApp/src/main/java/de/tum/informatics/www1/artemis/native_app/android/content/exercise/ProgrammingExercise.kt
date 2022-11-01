package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.Attachment
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("programming")
data class ProgrammingExercise(
    override val id: Int? = null,
    override val title: String? = null,
    override val shortName: String? = null,
    override val maxPoints: Float? = null,
    override val bonusPoints: Float? = null,
    override val releaseDate: Instant? = null,
    override val dueDate: Instant? = null,
    override val difficulty: Difficulty? = null,
    override val mode: Mode = Mode.INDIVIDUAL,
    override val categories: List<Category> = emptyList(),
    override val visibleToStudents: Boolean? = null,
    override val teamMode: Boolean? = null,
    override val participationStatus: ParticipationStatus? = null,
    override val problemStatement: String? = null,
    override val assessmentType: AssessmentType? = null,
    override val allowComplaintsForAutomaticAssessments: Boolean? = null,
    override val allowManualFeedbackRequests: Boolean? = null,
    override val includedInOverallScore: IncludedInOverallScore = IncludedInOverallScore.INCLUDED_COMPLETELY,
    override val exampleSolutionPublicationDate: Instant? = null,
    override val attachments: List<Attachment> = emptyList(),
    override val studentParticipations: List<Participation>? = null,
    val programmingLanguage: ProgrammingLanguage? = null
) : Exercise() {

    override fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise = copy(studentParticipations = newParticipations)

    enum class ProgrammingLanguage {
        JAVA,
        PYTHON,
        C,
        HASKELL,
        KOTLIN,
        VHDL,
        ASSEMBLER,
        SWIFT,
        OCAML,
        EMPTY
    }
}