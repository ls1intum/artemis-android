package de.tum.informatics.www1.artemis.native_app.feature.lecture_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.lecture.Lecture
import de.tum.informatics.www1.artemis.native_app.feature.lecture_view.service.LectureService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

internal class LectureViewModel(
    private val lectureId: Long,
    private val networkStatusProvider: NetworkStatusProvider,
    private val lectureService: LectureService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService
) : ViewModel() {

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

    fun requestReloadLecture() {
        onReloadLecture.tryEmit(Unit)
    }
}