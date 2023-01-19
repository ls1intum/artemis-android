package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.lecture_units.LectureUnit
import de.tum.informatics.www1.artemis.native_app.core.websocket.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.LectureService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class LectureViewModel(
    private val lectureId: Long,
    private val networkStatusProvider: NetworkStatusProvider,
    private val lectureService: LectureService,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    serverTimeService: ServerTimeService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

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
     * Only emits the lecture units that are already released.
     */
    private val visibleLectureUnits: Flow<List<LectureUnit>> =
        transformLatest(
            lectureDataState.filterSuccess(),
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

    val lectureUnits: Flow<List<LectureUnit>> = combine(
        visibleLectureUnits,
        lectureUnitCompletedMap
    ) { visibleLectureUnits, lectureUnitCompletedMap ->
        visibleLectureUnits.map { lectureUnit ->
            val isCompleted = lectureUnitCompletedMap[lectureUnit.id] ?: lectureUnit.completed
            lectureUnit.withCompleted(isCompleted)
        }
    }

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
    ) {
        viewModelScope.launch {
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