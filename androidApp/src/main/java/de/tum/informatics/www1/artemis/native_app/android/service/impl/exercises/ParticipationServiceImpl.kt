package de.tum.informatics.www1.artemis.native_app.android.service.impl.exercises

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.participation.StudentParticipation
import de.tum.informatics.www1.artemis.native_app.android.content.exercise.submission.Submission
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ParticipationService
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ParticipationService.ProgrammingSubmissionStateData
import de.tum.informatics.www1.artemis.native_app.android.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.android.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.android.service.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * From: https://github.com/ls1intum/Artemis/blob/5c13e2e1b5b6d81594b9123946f040cbf6f0cfc6/src/main/webapp/app/overview/participation-websocket.service.ts
 */
class ParticipationServiceImpl(
    private val websocketProvider: WebsocketProvider,
    private val ktorProvider: KtorProvider,
    private val serverCommunicationProvider: ServerCommunicationProvider,
    private val networkStatusProvider: NetworkStatusProvider,
    private val accountService: AccountService,
    private val jsonProvider: JsonProvider
) :
    ParticipationService {

    companion object Foo {
        private const val PERSONAL_PARTICIPATION_TOPIC = "/user/topic/newResults"
        private const val PERSONAL_NEW_SUBMISSIONS_TOPIC = "/user/topic/newSubmissions"
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

    @OptIn(DelicateCoroutinesApi::class)
    private val personalNewSubmissionsUpdater: Flow<WebsocketProgrammingSubmissionMessage> =
        websocketProvider.subscribe(
            PERSONAL_NEW_SUBMISSIONS_TOPIC,
            WebsocketProgrammingSubmissionMessage.serializer()
        )
            .shareIn(
                scope = GlobalScope,
                started = SharingStarted.WhileSubscribed(replayExpiration = Duration.ZERO),
                replay = 1
            )

    /**
     * Subscribe for the latest pending submission for the given participation.
     * A latest pending submission is characterized by the following properties:
     * - Submission is the newest one (by submissionDate)
     * - Submission does not have a result (yet)
     * - Submission is not older than DEFAULT_EXPECTED_RESULT_ETA (in this case it could be that never a result will come due to an error)
     *
     * Will emit:
     * - A submission if a last pending submission exists.
     * - An null value when there is not a pending submission.
     * - An null value when no result arrived in time for the submission.
     *
     * This method will execute a REST call to the server so that the subscriber will always receive the latest information from the server.
     *
     * @param participationId id of the ProgrammingExerciseStudentParticipation
     * @param exerciseId id of ProgrammingExercise
     * @param personal whether the current user is a participant in the participation.
     * @param fetchPending whether the latest pending submission should be fetched from the server
     */
    override fun getLatestPendingSubmissionByParticipationIdFlow(
        participationId: Int,
        exerciseId: Int,
        isPersonalParticipation: Boolean,
        personal: Boolean,
        fetchPending: Boolean
    ): Flow<ProgrammingSubmissionStateData?> {
        /*
        This implementation works differently than the one provided on the web-app. However, the final result is the same.
         */

        //Flow that emits when the websocket sends new data
        val updatingFlow: Flow<ProgrammingSubmissionStateData?> =
            if (personal) personalNewSubmissionsUpdater else {
                websocketProvider.subscribe(
                    "/topic/exercise/$exerciseId/newSubmissions",
                    WebsocketProgrammingSubmissionMessage.serializer()
                )
            }.transformLatest { message ->
                when (message) {
                    is WebsocketProgrammingSubmissionMessage.Error -> emit(
                        ProgrammingSubmissionStateData.FailedSubmission(
                            message.participationId ?: 0
                        )
                    )
                    is WebsocketProgrammingSubmissionMessage.ReceivedSubmission -> {
                        val submission = message.submission
                        emit(
                            ProgrammingSubmissionStateData.IsBuildingPendingSubmission(
                                participationId,
                                submission
                            )
                        )

                        val remainingTime = getExpectedRemainingTimeForBuild(submission)
                        //If a result is not available within the remaining time, null is returned.
                        val result: Unit? = withTimeoutOrNull(remainingTime) {
                            //Wait for the submission updater to emit a result for the participation we are interested in
                            val submissionUpdater = if (personal) personalSubmissionUpdater else {
                                websocketProvider.subscribe(
                                    exerciseParticipationTopic(exerciseId),
                                    Submission.serializer()
                                )
                            }

                            submissionUpdater
                                .filter { it.participation?.id == participationId }
                                .map { } // Map to Unit, we are not interested in the value
                                .first()
                        }

                        if (result == null) {
                            // The server sends the latest submission without a result - so it could be that the result is too old. In this case the error is shown directly.
                            emit(
                                ProgrammingSubmissionStateData.FailedSubmission(participationId)
                            )
                        } else {
                            //The server has sent the result.
                            emit(
                                ProgrammingSubmissionStateData.NoPendingSubmission(participationId)
                            )
                        }
                    }
                }

            }

        return if (fetchPending) {
            val initialFlow = fetchLatestPendingSubmissionByParticipationId(participationId)
                .map { submission ->
                    ProgrammingSubmissionStateData.IsBuildingPendingSubmission(
                        participationId,
                        submission
                    )
                }
            merge(initialFlow, updatingFlow)
        } else updatingFlow
    }

    /**
     * Fetch the latest pending submission for a participation, which means:
     * - Submission is the newest one (by submissionDate)
     * - Submission does not have a result (yet)
     * - Submission is not older than DEFAULT_EXPECTED_RESULT_ETA (in this case it could be that never a result will come due to an error)
     *
     * @param participationId
     */
    private fun fetchLatestPendingSubmissionByParticipationId(participationId: Int): Flow<Submission> {
        return combine(
            serverCommunicationProvider.serverUrl,
            accountService.authenticationData
        ) { a, b -> a to b }
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                                performNetworkCall {
                                    val bodyText = ktorProvider.ktorClient.get(serverUrl) {
                                        url {
                                            appendPathSegments(
                                                "api",
                                                "programming-exercise-participations",
                                                participationId.toString(),
                                                "latest-pending-submission"
                                            )
                                        }

                                        contentType(ContentType.Any)
                                        bearerAuth(authData.authToken)
                                    }.bodyAsText()

                                    if (bodyText.isNotEmpty()) {
                                        jsonProvider.networkJsonConfiguration.decodeFromString<Submission?>(
                                            bodyText
                                        )
                                    } else null
                                }
                            }
                        )
                    }
                    AccountService.AuthenticationData.NotLoggedIn -> {
                    }
                }
            }
            .filter { it is DataState.Success<*> }
            .map { it.orThrow() }
            .filterNotNull()
    }

    private fun getExpectedRemainingTimeForBuild(submission: Submission): Duration {
        return 2.minutes - (Clock.System.now() - (submission.submissionDate ?: Clock.System.now()))
    }

    override fun subscribeForParticipationChanges(): Flow<StudentParticipation> = emptyFlow()

    @Serializable
    private data class ProgrammingSubmissionError(val error: String, val participationId: Int)

    @Serializable(with = WebsocketProgrammingSubmissionMessage.Deserializer::class)
    private sealed class WebsocketProgrammingSubmissionMessage {
        @Serializable
        class Error(val error: String, val participationId: Int?) :
            WebsocketProgrammingSubmissionMessage()

        @Serializable(with = ReceivedSubmission.Deserializer::class)
        class ReceivedSubmission(val submission: Submission) :
            WebsocketProgrammingSubmissionMessage() {
            object Deserializer : KSerializer<ReceivedSubmission> {

                @OptIn(ExperimentalSerializationApi::class)
                override val descriptor: SerialDescriptor =
                    SerialDescriptor("ReceivedSubmission", Submission.serializer().descriptor)

                override fun deserialize(decoder: Decoder): ReceivedSubmission {
                    return ReceivedSubmission(decoder.decodeSerializableValue(Submission.serializer()))
                }

                override fun serialize(encoder: Encoder, value: ReceivedSubmission) =
                    throw NotImplementedError()

            }
        }

        object Deserializer :
            JsonContentPolymorphicSerializer<WebsocketProgrammingSubmissionMessage>(
                WebsocketProgrammingSubmissionMessage::class
            ) {
            override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out WebsocketProgrammingSubmissionMessage> {
                return when {
                    "error" in element.jsonObject -> Error.serializer()
                    else -> ReceivedSubmission.serializer()
                }
            }
        }
    }
}