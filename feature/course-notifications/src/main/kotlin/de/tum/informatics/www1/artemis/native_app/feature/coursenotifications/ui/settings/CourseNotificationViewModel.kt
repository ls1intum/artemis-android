package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.CourseNotification
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.course_notification_model.NotificationCategory
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class CourseNotificationViewModel(
    private val courseId: Long,
    private val courseNotificationService: CourseNotificationService,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    private val notificationState: StateFlow<DataState<CourseNotification>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            requestReload.onStart { emit(Unit) }
        ){ a, b, _ -> a to b }
            .flatMapLatest { (serverUrl, authToken) ->
                retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                    courseNotificationService.loadCourseNotifications(
                        courseId = courseId
                    )
                }

        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    val communicationNotifications: StateFlow<List<CourseNotification>> =
        notificationState
            .filterIsInstance<DataState.Success<List<CourseNotification>>>()
            .map { state -> state.data.filter { it.category == NotificationCategory.COMMUNICATION } }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    val generalNotifications: StateFlow<List<CourseNotification>> =
        notificationState
            .filterIsInstance<DataState.Success<List<CourseNotification>>>()
            .map { state -> state.data.filter { it.category == NotificationCategory.GENERAL } }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())
}
