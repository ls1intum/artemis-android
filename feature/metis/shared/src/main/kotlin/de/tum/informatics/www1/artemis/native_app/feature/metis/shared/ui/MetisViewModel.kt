package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

/**
 * Base view model which handles logic such as creating posts and reactions.
 */
abstract class MetisViewModel(
    courseService: CourseService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    websocketProvider: WebsocketProvider,
    coroutineContext: CoroutineContext,
    private val courseId: Long
) : ReloadableViewModel() {

    val course: StateFlow<DataState<Course>> = courseService.performAutoReloadingNetworkCall(
        networkStatusProvider = networkStatusProvider,
        manualReloadFlow = requestReload
    ) {
        getCourse(courseId).bind { it.course }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val accountDataStateFlow: Flow<DataState<Account>> = accountDataService
        .performAutoReloadingNetworkCall(
            networkStatusProvider = networkStatusProvider,
            manualReloadFlow = requestReload
        ) {
            getAccountData()
        }

    val isAtLeastTutorInCourse: StateFlow<Boolean> = combine(
        accountDataStateFlow,
        course,
    ) { accountDataState, courseDataState ->
        accountDataState.join(courseDataState).bind { (account, course) ->
            account.isAtLeastTutorInCourse(course = course)
        }
            .orElse(false)
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    // Emits when a reload is manually requested or when we have a websocket reconnect
    val onReloadRequestAndWebsocketReconnect = merge(
        requestReload,
        websocketProvider
            .connectionState
            .withPrevious()
            .filter { (prevConnection, nowConnection) ->
                prevConnection != null && !prevConnection.isConnected && nowConnection.isConnected
            }
            .map { } // Convert to Unit
    )
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 0)
        .onStart { emit(Unit) }

    val clientId: StateFlow<DataState<Long>> = accountDataService.performAutoReloadingNetworkCall(
        networkStatusProvider = networkStatusProvider,
        manualReloadFlow = requestReload
    ) {
        getAccountData().bind { it.id }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily, DataState.Loading())

    val clientIdOrDefault: StateFlow<Long> = clientId
        .map { it.orElse(0L) }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Lazily, 0L)
}
