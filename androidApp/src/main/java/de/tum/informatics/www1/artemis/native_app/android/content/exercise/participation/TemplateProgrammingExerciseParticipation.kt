package de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation

import de.tum.informatics.www1.artemis.native_app.android.content.Team
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.ProgrammingExercise
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Result
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("template")
class TemplateProgrammingExerciseParticipation(
    override val id: Int? = null,
    override val initializationState: InitializationState? = null,
    override val initializationDate: Instant? = null,
    override val individualDueDate: Instant? = null,
    override val results: List<Result>? = null,
    override val exercise: Exercise? = null,
    val programmingExercise: ProgrammingExercise? = null,
    val repositoryUrl: String? = null,
    val buildPlanId: String? = null,
    val buildPlanUrl: String? = null
) : Participation()