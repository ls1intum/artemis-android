package de.tum.informatics.www1.artemis.native_app.core.model.exercise

import de.tum.informatics.www1.artemis.native_app.core.common.isInFutureFlow
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.ProgrammingExerciseStudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Attachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("programming")
data class ProgrammingExercise(
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
    val programmingLanguage: ProgrammingLanguage? = null,
    val buildAndTestStudentSubmissionsAfterDueDate: Instant? = null,
    val showTestNamesToStudents: Boolean? = false,
    val staticCodeAnalysisEnabled: Boolean = false,
    val maxStaticCodeAnalysisPenalty: Float? = null
) : Exercise() {

    override fun copyWithUpdatedParticipations(newParticipations: List<Participation>): Exercise =
        copy(studentParticipations = newParticipations)

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

/**
 * A result is preliminary if:
 * - The programming exercise buildAndTestAfterDueDate is set
 * - The submission date of the result / result completionDate is before the buildAndTestAfterDueDate
 *
 * @param latestResult Result with attached Submission - if submission is null, method will use the result completionDate as a reference.
 * @see [https://github.com/ls1intum/Artemis/blob/fe3a00a2118be74ecc7b2f7e85e223f175e509d2/src/main/webapp/app/exercises/programming/shared/utils/programming-exercise.utils.ts#L68]
 */
fun ProgrammingExercise.isResultPreliminary(latestResult: Result): Flow<Boolean> {
    if (latestResult.participation is ProgrammingExerciseStudentParticipation && latestResult.participation.testRun == true) {
        return flowOf(false)
    }

    if (latestResult.completionDate == null) {
        // in the unlikely case the completion date is not set yet (this should not happen), it is preliminary
        return flowOf(true)
    }

    // If an exercise's assessment type is not automatic the last result is supposed to be manually assessed
    if (assessmentType != Exercise.AssessmentType.AUTOMATIC) {
        // either the semi-automatic result is not yet available as last result (then it is preliminary), or it is already available (then it still can be changed)
        if (assessmentDueDate != null) {
            return assessmentDueDate.isInFutureFlow()
        }
        // in case the assessment due date is not set, the assessment type of the latest result is checked. If it is automatic the result is still preliminary.
        return flowOf(latestResult.assessmentType == Exercise.AssessmentType.AUTOMATIC)
    }
    // When the due date for the automatic building and testing is available but not reached, the result is preliminary
    if (buildAndTestStudentSubmissionsAfterDueDate != null) {
        return flowOf(latestResult.completionDate < buildAndTestStudentSubmissionsAfterDueDate)
    }
    return flowOf(false)
}