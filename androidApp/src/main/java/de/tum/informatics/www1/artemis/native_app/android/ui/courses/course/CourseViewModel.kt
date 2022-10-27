package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.CourseService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import kotlinx.coroutines.flow.*


class CourseViewModel(
    private val courseId: Int,
    serverCommunicationProvider: ServerCommunicationProvider,
    accountService: AccountService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val courseService: CourseService
) : ViewModel() {

    val course: Flow<DataState<Course>> =
        combine(
            serverCommunicationProvider.serverUrl,
            accountService.authenticationData
        ) { serverUrl, authData ->
            serverUrl to authData
        }
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            retryOnInternet(
                                networkStatusProvider.currentNetworkStatus,
                                minimumLoadingMillis = 4000
                            ) {
                                val r = courseService.getCourse(courseId, serverUrl, authData.authToken)
                                r
                            })
                    }
                    AccountService.AuthenticationData.NotLoggedIn -> {
                        emit(DataState.Suspended())
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

}