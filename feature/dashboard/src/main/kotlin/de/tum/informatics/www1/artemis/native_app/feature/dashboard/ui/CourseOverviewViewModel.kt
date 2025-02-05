package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * View model that fetches the dashboard from the model and supports reloading of the dashboard.
 */
internal class CourseOverviewViewModel(
    private val dashboardService: DashboardService,
    private val dashboardStorageService: DashboardStorageService,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    /**
     * Emit a unit to this flow, to reload the dashboard.
     */
    private val reloadDashboard = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _dashboardState = MutableStateFlow<DataState<Dashboard>>(DataState.Loading())
    val dashboard: StateFlow<DataState<Dashboard>> get() = _dashboardState

    // Load the dashboard on init and whenever the reloadDashboard flow emits a value.
    init {
        viewModelScope.launch {
            reloadDashboard.collect {
                loadDashboard()
            }
        }
        loadDashboard()
    }

    /**
     * Always emits the latest dashboard. Automatically updated when [requestReloadDashboard] is requested,
     * the login status changes or the server is updated.
     */
    private fun loadDashboard() {
        viewModelScope.launch {
            val authToken = accountService.authToken.first()
            val serverUrl = serverConfigurationService.serverUrl.first()
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                dashboardService.loadDashboard(authToken, serverUrl).bind { dashboard ->
                    val sortedDashboard = dashboard.copy(
                        courses = dashboard.courses.sortedBy { it.course.title }.toMutableList()
                    )
                    val finalDashboard = sortCoursesInSections(serverUrl, sortedDashboard)
                    finalDashboard
                }
            }.collect {
                _dashboardState.value = it
            }
        }
    }

    fun reorderCourses() {
        val currentState = _dashboardState.value

        if (currentState is DataState.Success) {
            val currentDashboard = currentState.data

            viewModelScope.launch {
                val finalDashboard = sortCoursesInSections(
                    serverConfigurationService.serverUrl.first(),
                    currentDashboard
                )
                _dashboardState.value = DataState.Success(finalDashboard)
            }
        }
    }

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
    private suspend fun sortCoursesInSections(
        serverUrl: String,
        currentDashboard: Dashboard
    ): Dashboard {
        if (currentDashboard.courses.size <= 5) {
            return currentDashboard
        }

        val recentlyAccessedCourseMap =
            dashboardStorageService.getLastAccesssedCourses(serverUrl).first()

        val coursesToMove = currentDashboard.courses.filter { course ->
            recentlyAccessedCourseMap.containsKey(course.course.id)
        }.toSet()
        val coursesToRemove = currentDashboard.recentCourses.filter { course ->
            !recentlyAccessedCourseMap.containsKey(course.course.id)
        }.toSet()

        if (coursesToMove.isEmpty() && coursesToRemove.isEmpty()) {
            return currentDashboard
        }

        val updatedCourses = currentDashboard.courses - coursesToMove + coursesToRemove
        val updatedRecentCourses = currentDashboard.recentCourses - coursesToRemove + coursesToMove
        return currentDashboard.copy(
            courses = updatedCourses.sortedBy { it.course.title }.toSet().toMutableList(),
            recentCourses = updatedRecentCourses.sortedBy { it.course.title }.toSet()
                .toMutableList()
        )
    }
}
