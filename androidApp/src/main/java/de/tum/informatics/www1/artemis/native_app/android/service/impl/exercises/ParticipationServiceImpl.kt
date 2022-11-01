package de.tum.informatics.www1.artemis.native_app.android.service.impl.exercises

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ParticipationService
import de.tum.informatics.www1.artemis.native_app.android.service.impl.WebsocketProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlin.time.Duration

/**
 * From: https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/overview/participation-websocket.service.ts
 */
class ParticipationServiceImpl(private val websocketProvider: WebsocketProvider) :
    ParticipationService {

    companion object Foo {
        private const val PERSONAL_PARTICIPATION_TOPIC = "/user/topic/newResults"
        private fun exerciseParticipationTopic(exerciseId: Int) =
            "/topic/exercise/${exerciseId}/newResults"
    }

    @OptIn(DelicateCoroutinesApi::class)
    override val personalSubmissionUpdater: Flow<Submission> =
        websocketProvider.subscribe(PERSONAL_PARTICIPATION_TOPIC, Submission.serializer())
            .shareIn(
                scope = GlobalScope,
                started = SharingStarted.WhileSubscribed(replayExpiration = Duration.ZERO),
                replay = 1
            )

    override fun getLatestPendingSubmissionByParticipationIdFlow(
        participationId: Int,
        exerciseId: Int,
        isPersonalParticipation: Boolean
    ): Flow<Submission> {
        val topic =
            if (isPersonalParticipation) PERSONAL_PARTICIPATION_TOPIC else exerciseParticipationTopic(
                exerciseId
            )

        return websocketProvider.subscribe(topic, Submission.serializer())
            .filter { it.participation?.id == participationId }
    }

    override fun subscribeForParticipationChanges(): Flow<StudentParticipation> = emptyFlow()
}