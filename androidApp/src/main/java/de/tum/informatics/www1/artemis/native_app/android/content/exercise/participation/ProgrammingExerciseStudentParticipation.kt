package de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation

import de.tum.informatics.www1.artemis.native_app.android.content.Team
import de.tum.informatics.www1.artemis.native_app.android.content.account.User
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Result
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("programming")
class ProgrammingExerciseStudentParticipation(
    override val id: Int? = null,
    override val initializationState: InitializationState? = null,
    override val initializationDate: Instant? = null,
    override val individualDueDate: Instant? = null,
    override val results: List<Result>? = null,
    override val exercise: Exercise? = null,
    override val type: ParticipationType? = null,
    override val student: User? = null,
    override val team: Team? = null,
    override val participantIdentifier: String? = null,
    override val testRun: Boolean? = null,
    val repositoryUrl: String? = null,
    val buildPlanId: String? = null
) : StudentParticipation()