package de.tum.informatics.www1.artemis.native_app.feature.lectureview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnitExercise
import de.tum.informatics.www1.artemis.native_app.core.ui.authTokenStateFlow
import de.tum.informatics.www1.artemis.native_app.core.ui.exercise.BaseExerciseListViewModel
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.core.websocket.LiveParticipationService
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.service.LectureService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class LectureViewModel(
    private val lectureId: Long,
    private val networkStatusProvider: NetworkStatusProvider,
    private val lectureService: LectureService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val liveParticipationService: LiveParticipationService,
    private val savedStateHandle: SavedStateHandle,
    serverTimeService: ServerTimeService,
    courseExerciseService: CourseExerciseService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : BaseExerciseListViewModel(courseExerciseService) {

    companion object {
        private const val LECTURE_UNIT_COMPLETED_MAP_TAG = "LECTURE_UNIT_COMPLETED_MAP_TAG"
    }

    private val lectureUnitCompletedMap: Flow<Map<Long, Boolean>> =
        savedStateHandle.getStateFlow(LECTURE_UNIT_COMPLETED_MAP_TAG, emptyMap())

    private val onReloadLecture = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val lectureDataState: StateFlow<DataState<Lecture>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            onReloadLecture.onStart { emit(Unit) }
        ) { a, b, _ -> a to b }
            .flatMapLatest { (serverUrl, authToken) ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    lectureService.loadLecture(
                        lectureId, serverUrl, authToken
                    )
                }
            }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)
    val authToken: StateFlow<String> = authTokenStateFlow(accountService)

    /**
     * The lecture with updated participations as they arrive.
     */
    private val latestLectureDataState: StateFlow<DataState<Lecture>> =
        lectureDataState.transformLatest { lectureDataState ->
            when (lectureDataState) {
                is DataState.Success -> {
                    emit(lectureDataState)

                    val currentLectureUnits = lectureDataState.data.lectureUnits.toMutableList()

                    // Start listening for participation updates
                    merge(
                        liveParticipationService
                            .personalSubmissionUpdater
                            .mapFilterToNewParticipationData(),
                        startedExerciseFlow
                    ).collect { newParticipationData ->
                        // Find lecture unit that contains the exercise
                        val lectureUnitIndex = currentLectureUnits.indexOfFirst { lectureUnit ->
                            val exercise = (lectureUnit as? LectureUnitExercise)?.exercise
                            exercise != null && exercise.id == newParticipationData.exerciseId
                        }

                        // This quite definitely should never happen, because newParticipationData comes from a click on a lecture unit
                        if (lectureUnitIndex == -1) return@collect

                        val lectureUnit =
                            currentLectureUnits[lectureUnitIndex] as? LectureUnitExercise
                                ?: return@collect // Should not happen

                        val lectureUnitExercise = lectureUnit.exercise
                            ?: return@collect // Should not happen

                        val newLectureUnit = lectureUnit.copy(
                            exercise = lectureUnitExercise.withUpdatedParticipation(
                                newParticipationData.newParticipation
                            )
                        )

                        currentLectureUnits[lectureUnitIndex] = newLectureUnit
                        emit(DataState.Success(lectureDataState.data.copy(lectureUnits = currentLectureUnits)))
                    }
                }

                else -> emit(lectureDataState)
            }
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val serverClock: Flow<Clock> = serverTimeService.onReloadRequired.flatMapLatest {
        serverTimeService.getServerClock()
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.WhileSubscribed(1.seconds), replay = 1)

    /**
     * Only emits the lecture units that are already released.
     */
    private val visibleLectureUnits: Flow<List<LectureUnit>> =
        transformLatest(
            latestLectureDataState.filterSuccess(),
            serverClock
        ) { lecture, serverClock ->
            val now = serverClock.now()

            while (true) {
                // Find the release date that will happen next
                val nextRelease = lecture
                    .lectureUnits
                    .mapNotNull { it.releaseDate }
                    .filter { it > now }
                    .minOrNull()

                if (nextRelease != null) {
                    // Emit those which have already been released
                    emit(lecture.lectureUnits.filter {
                        val releaseDate = it.releaseDate
                        releaseDate == null || releaseDate < now
                    })

                    // Wait for the next release
                    val waitingTime = nextRelease - now
                    delay(waitingTime + 500.milliseconds)
                } else {
                    // Emit the current values and finish
                    emit(lecture.lectureUnits)
                    break
                }
            }
        }
            .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    val lectureUnits: StateFlow<List<LectureUnit>> = combine(
        visibleLectureUnits,
        lectureUnitCompletedMap
    ) { visibleLectureUnits, lectureUnitCompletedMap ->
        visibleLectureUnits.map { lectureUnit ->
            val isCompleted = lectureUnitCompletedMap[lectureUnit.id] ?: lectureUnit.completed
            lectureUnit.withCompleted(isCompleted)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch(coroutineContext) {
            // Fill the completed map with the values from the http request
            lectureDataState.filterSuccess().collect { lecture ->
                savedStateHandle[LECTURE_UNIT_COMPLETED_MAP_TAG] =
                    lecture.lectureUnits.associate { it.id to it.completed }
            }
        }
    }

    fun requestReloadLecture() {
        onReloadLecture.tryEmit(Unit)
    }

    fun updateLectureUnitIsComplete(
        lectureUnitId: Long,
        isCompleted: Boolean
    ): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            lectureService.completeLectureUnit(
                lectureUnitId = lectureUnitId,
                lectureId = lectureId,
                completed = isCompleted,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .onSuccess {
                    val newIsCompletedMap = lectureUnitCompletedMap
                        .first()
                        .toMutableMap()

                    newIsCompletedMap[lectureUnitId] = isCompleted
                    savedStateHandle[LECTURE_UNIT_COMPLETED_MAP_TAG] = newIsCompletedMap
                }
                .bind { true }
                .or(false)
        }
    }
}
