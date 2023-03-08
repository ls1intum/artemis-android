package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.*
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseExerciseService
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
import de.tum.informatics.www1.artemis.native_app.core.websocket.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.LectureService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class LectureViewModel(
    private val lectureId: Long,
    private val networkStatusProvider: NetworkStatusProvider,
    private val lectureService: LectureService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    serverTimeService: ServerTimeService,
    private val savedStateHandle: SavedStateHandle,
    courseExerciseService: CourseExerciseService,
    private val liveParticipationService: LiveParticipationService,

    ) : BaseExerciseListViewModel(serverConfigurationService, accountService, courseExerciseService) {

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
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

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
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * Only emits the lecture units that are already released.
     */
    private val visibleLectureUnits: Flow<List<LectureUnit>> =
        transformLatest(
            latestLectureDataState.filterSuccess(),
            serverTimeService.serverClock
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
            .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val lectureUnits: StateFlow<List<LectureUnit>> = combine(
        visibleLectureUnits,
        lectureUnitCompletedMap
    ) { visibleLectureUnits, lectureUnitCompletedMap ->
        visibleLectureUnits.map { lectureUnit ->
            val isCompleted = lectureUnitCompletedMap[lectureUnit.id] ?: lectureUnit.completed
            lectureUnit.withCompleted(isCompleted)
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)
    val authToken: StateFlow<String> = authTokenStateFlow(accountService)

    init {
        viewModelScope.launch {
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
        isCompleted: Boolean,
        onResponse: (isSuccessful: Boolean) -> Unit
    ): Job {
        return viewModelScope.launch {
            val isSuccessful = lectureService.completeLectureUnit(
                lectureUnitId = lectureUnitId,
                lectureId = lectureId,
                completed = isCompleted,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            ) is NetworkResponse.Response

            if (isSuccessful) {
                val newIsCompletedMap = lectureUnitCompletedMap
                    .first()
                    .toMutableMap()

                newIsCompletedMap[lectureUnitId] = isCompleted
                savedStateHandle[LECTURE_UNIT_COMPLETED_MAP_TAG] = newIsCompletedMap
            }

            onResponse(isSuccessful)
        }
    }
}
