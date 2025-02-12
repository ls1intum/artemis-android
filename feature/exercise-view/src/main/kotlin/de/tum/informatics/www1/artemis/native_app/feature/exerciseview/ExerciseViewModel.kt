package de.tum.informatics.www1.artemis.native_app.feature.exerciseview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.ui.authTokenStateFlow
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class ExerciseViewModel(
    private val exerciseId: Long,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val exerciseService: ExerciseService,
    private val liveParticipationService: LiveParticipationService,
    private val courseExerciseService: CourseExerciseService,
    private val channelService: ChannelService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val requestReloadExercise = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Flow that holds (serverUrl, authData) and emits every time the exercise should be reloaded.
     */
    private val baseConfigurationFlow: Flow<Pair<String, String>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            requestReloadExercise.onStart { emit(Unit) }
        ) { serverUrl, authData, _ ->
            serverUrl to authData
        }
            .flowOn(coroutineContext)
            .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val fetchedExercise: Flow<DataState<Exercise>> = flatMapLatest(
            exerciseService.onReloadRequired,
            requestReloadExercise.onStart { emit(Unit) }
        ) { _, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                exerciseService.getExerciseDetails(exerciseId)
            }
        }
        .flowOn(coroutineContext)
        .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Emitted to when startExercise is successful
     */
    private val _gradedParticipation = MutableSharedFlow<Participation>()

    /**
     * Emits the exercise updated with the latest participations and results
     */
    val exerciseDataState: StateFlow<DataState<Exercise>> =
        fetchedExercise
            .transformLatest { exercise ->
                when (exercise) {
                    is DataState.Success -> {
                        var currentExercise = exercise.data

                        emit(exercise)

                        val newParticipationsFlow =
                            merge(
                                liveParticipationService
                                    .personalSubmissionUpdater.filter { result -> result.participation?.exercise?.id == exerciseId }
                                    .mapNotNull { it.participation },
                                _gradedParticipation
                            )

                        newParticipationsFlow.collect { participation ->
                            currentExercise =
                                currentExercise.withUpdatedParticipation(participation)

                            emit(DataState.Success(currentExercise))
                        }
                    }

                    else -> emit(exercise)
                }
            }
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * The latest result for the exercise.
     */
    val latestResultDataState: StateFlow<DataState<Result?>> =
        exerciseDataState.map { exerciseData ->
            exerciseData.bind { exercise ->
                val participation =
                    exercise.getSpecificStudentParticipation(false)

                participation?.results.orEmpty().sortedByDescending { it.completionDate }
                    .firstOrNull()
            }
        }
            .flowOn(coroutineContext)
            .stateIn(viewModelScope, SharingStarted.Lazily)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)
    val authToken: StateFlow<String> = authTokenStateFlow(accountService)

    val channelDataState: StateFlow<DataState<ChannelChat>> = exerciseDataState
        .flatMapLatest { exerciseState ->
            when (exerciseState) {
                is DataState.Success -> {
                    val courseId = exerciseState.data.course?.id.let { it ?: 0L }
                    baseConfigurationFlow.flatMapLatest { (serverUrl, authToken) ->
                        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                            channelService.getExerciseChannel(exerciseId, courseId, serverUrl, authToken)
                        }
                    }
                }
                else -> flowOf(DataState.Loading())
            }
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)


    fun requestReloadExercise() {
        requestReloadExercise.tryEmit(Unit)
    }

    /**
     * Deferred returns the participation id if starting was successful.
     */
    fun startExercise(): Deferred<Long?> {
        return viewModelScope.async(coroutineContext) {
            val response = courseExerciseService.startExercise(exerciseId)

            response
                .onSuccess {
                    _gradedParticipation.emit(it)
                }
                .bind { it.id ?: 0L }
                .orNull()
        }
    }
}
