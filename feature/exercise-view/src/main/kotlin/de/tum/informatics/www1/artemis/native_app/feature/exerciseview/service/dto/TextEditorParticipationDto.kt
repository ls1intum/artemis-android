package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.service.dto

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.TextSubmission
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * The answer of "text/participations/{participationId}/text-editor".
 *
 * Read into its own type rather than straight into [Participation], because the submissions there
 * carry no "submissionExerciseType". The polymorphic submission model needs that discriminator, and
 * the participation endpoint does send it, so decoding this response as a participation yields a
 * submission the text editor cannot read and an editor with no text in it.
 */
@Serializable
internal data class TextEditorParticipationDto(
    val id: Long? = null,
    val initializationState: Participation.InitializationState? = null,
    val initializationDate: Instant? = null,
    val individualDueDate: Instant? = null,
    val exercise: Exercise? = null,
    val submissions: List<TextEditorSubmissionDto> = emptyList()
) {

    fun toParticipation(): Participation = StudentParticipation.StudentParticipationImpl(
        id = id,
        initializationState = initializationState,
        initializationDate = initializationDate,
        individualDueDate = individualDueDate,
        exercise = exercise,
        submissions = submissions.map { it.toSubmission() }
    )
}

@Serializable
internal data class TextEditorSubmissionDto(
    val id: Int? = null,
    val submitted: Boolean? = null,
    val submissionDate: Instant? = null,
    val text: String? = null,
    val results: List<Result>? = null
) {

    fun toSubmission(): TextSubmission = TextSubmission(
        id = id,
        submitted = submitted,
        submissionDate = submissionDate,
        text = text,
        results = results
    )
}
