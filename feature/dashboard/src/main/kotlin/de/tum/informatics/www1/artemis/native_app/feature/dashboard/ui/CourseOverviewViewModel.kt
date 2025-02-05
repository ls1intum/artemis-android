package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardStorageService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * View model that fetches the dashboard from the model and supports reloading of the dashboard.
 */
internal class CourseOverviewViewModel(
    private val dashboardService: DashboardService,
    private val dashboardStorageService: DashboardStorageService,
    accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    /**
     * Emit a unit to this flow, to reload the dashboard.
     */
    private val reloadDashboard = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Always emits the latest dashboard. Automatically updated when [requestReloadDashboard] is requested,
     * the login status changes or the server is updated.
     */
    val dashboard: StateFlow<DataState<Dashboard>> =
        flatMapLatest(
            accountService.authToken,
            serverConfigurationService.serverUrl,
            reloadDashboard.onStart { emit(Unit) }
        ) { authToken, serverUrl, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                dashboardService.loadDashboard(
                    authToken,
                    serverUrl
                ).bind { dashboard ->
                    val sortedDashboard = dashboard.copy(courses = dashboard.courses.sortedBy { it.course.title }.toMutableList())
                    sortCoursesInSections(serverUrl, sortedDashboard)
                }
            }
        }
            //Store the loaded dashboard, so it is not loaded again when somebody collects this flow.
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Request a reload of the dashboard.
     */
    fun requestReloadDashboard() {
        reloadDashboard.tryEmit(Unit)
    }

    suspend fun onCourseAccessed(courseId: Long) {
        val serverUrl = serverConfigurationService.serverUrl.first()
        dashboardStorageService.onCourseAccessed(
            courseId = courseId,
            serverHost = serverUrl
        )
    }

    /**
     * Checks for recently accessed courses and adds them to the recent courses section.
     */
    private suspend fun sortCoursesInSections(serverUrl: String, currentDashboard: Dashboard): Dashboard {
        if (currentDashboard.courses.size <= 5) {
            return currentDashboard
        }

        val recentlyAccessedCourseMap = dashboardStorageService.getLastAccesssedCourses(serverUrl).first()
        val coursesToMove = currentDashboard.courses.filter { course ->
            recentlyAccessedCourseMap.containsKey(course.course.id)
        }.sortedBy { it.course.title }.toSet()

        if (coursesToMove.isEmpty()) {
            return currentDashboard
        }

        val updatedCourses = currentDashboard.courses - coursesToMove
        val updatedRecentCourses = currentDashboard.recentCourses + coursesToMove
        return currentDashboard.copy(
            courses = updatedCourses.toMutableList(),
            recentCourses = updatedRecentCourses.toMutableList()
        )
    }
}
