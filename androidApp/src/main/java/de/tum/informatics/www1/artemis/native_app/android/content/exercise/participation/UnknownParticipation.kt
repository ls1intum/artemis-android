package de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Result
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Used if the actual type of participation is unknown
 */
@Serializable
class UnknownParticipation(
    override val id: Int? = null,
    override val initializationState: InitializationState? = null,
    override val initializationDate: Instant? = null,
    override val individualDueDate: Instant? = null,
    override val results: List<Result>? = null,
    override val exercise: Exercise? = null,
) : Participation()