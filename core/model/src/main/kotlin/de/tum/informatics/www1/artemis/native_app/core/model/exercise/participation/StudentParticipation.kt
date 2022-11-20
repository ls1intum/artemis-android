package de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation

import de.tum.informatics.www1.artemis.native_app.core.model.Team
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class StudentParticipation : Participation() {
    abstract val student: User?
    abstract val team: Team?
    abstract val participantIdentifier: String?
    abstract val testRun: Boolean?

    /**
     * Kotlin serialization cannot handle constructor arguments of superclasses.
     * Therefore, this weird inheritance is needed.
     */
    @Serializable
    @SerialName("student")
    class StudentParticipationImpl(
        override val id: Int? = null,
        override val initializationState: InitializationState? = null,
        override val initializationDate: Instant? = null,
        override val individualDueDate: Instant? = null,
        override val results: List<Result>? = null,
        override val exercise: Exercise? = null,
        override val student: User? = null,
        override val team: Team? = null,
        override val participantIdentifier: String? = null,
        override val testRun: Boolean? = null,
        override val submissions: List<Submission>? = null,
    ) : StudentParticipation()
}