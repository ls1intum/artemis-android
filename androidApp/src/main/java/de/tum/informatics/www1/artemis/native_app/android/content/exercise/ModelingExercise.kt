package de.tum.informatics.www1.artemis.native_app.android.content.exercise

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.android.content.lecture.attachment.Attachment
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("modeling")
data class ModelingExercise(
    override val id: Int? = null,
    override val title: String? = null,
    override val shortName: String? = null,
    override val maxPoints: Float? = null,
    override val bonusPoints: Float? = null,
    override val releaseDate: Instant? = null,
    override val assessmentDueDate: Instant? = null,
    override val dueDate: Instant? = null,
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
    val diagramType: UMLDiagramType? = null,
    val exampleSolutionModel: String? = null,
    val exampleSolutionExplanation: String? = null
) : Exercise() {
    enum class UMLDiagramType {
        ClassDiagram,
        ObjectDiagram,
        ActivityDiagram,
        UseCaseDiagram,
        CommunicationDiagram,
        ComponentDiagram,
        DeploymentDiagram,
        PetriNet,
        SyntaxTree,
        Flowchart
    }

    override fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise = copy(studentParticipations = newParticipations)
}