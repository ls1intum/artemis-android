package de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("solution")
class SolutionProgrammingExerciseParticipation(
    override val id: Long? = null,
    override val initializationState: InitializationState? = null,
    override val initializationDate: Instant? = null,
    override val individualDueDate: Instant? = null,
    override val results: List<Result>? = null,
    override val exercise: Exercise? = null,
    override val submissions: List<Submission>? = null,
    val programmingExercise: ProgrammingExercise? = null,
    val repositoryUrl: String? = null,
    val buildPlanId: String? = null,
    val buildPlanUrl: String? = null
) : Participation()