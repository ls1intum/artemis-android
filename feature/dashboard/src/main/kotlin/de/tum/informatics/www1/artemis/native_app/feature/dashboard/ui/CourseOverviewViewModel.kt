package de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.CourseWithScore
import de.tum.informatics.www1.artemis.native_app.core.model.Dashboard
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardService
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.DashboardStorageService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
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
    private val courseAccess = MutableSharedFlow<Long>(extraBufferCapacity = 1)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    val sorting = MutableStateFlow(CourseSorting.ALPHABETICAL_ASCENDING)

    private val _dashboardState = MutableStateFlow<DataState<Dashboard>>(DataState.Loading())
    val dashboard: StateFlow<DataState<Dashboard>> = combine(_dashboardState, query, sorting) { dashboardState, query, sorting ->
        if (dashboardState is DataState.Success) {
            val originalDashboard = dashboardState.data

            val filteredCourses = originalDashboard.courses.filter {
                it.course.title.contains(query, ignoreCase = true)
            }.toMutableList()
            val filteredRecentCourses = originalDashboard.recentCourses.filter {
                it.course.title.contains(query, ignoreCase = true)
            }.toMutableList()

            val sortedCourses = sortCourses(filteredCourses, sorting)
            val sortedRecentCourses = sortCourses(filteredRecentCourses, sorting)

            DataState.Success(Dashboard(courses = sortedCourses, recentCourses = sortedRecentCourses))
        } else {
            dashboardState
        }
    }.stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Load the dashboard on init and whenever the reloadDashboard flow emits a value.
     * Trigger a reordering of the courses when a course is accessed.
     */
    init {
        viewModelScope.launch(coroutineContext) {
            courseAccess.collect {
                val currentState = _dashboardState.value

                if (currentState is DataState.Success) {
                    val currentDashboard = currentState.data
                    val finalDashboard = extractCoursesInSections(
                        serverConfigurationService.serverUrl.first(),
                        currentDashboard
                    )
                    _dashboardState.value = DataState.Success(finalDashboard)
                }
            }
        }

        viewModelScope.launch(coroutineContext) {
            reloadDashboard.collect {
                loadDashboard(coroutineContext)
            }
        }

        loadDashboard(coroutineContext)
    }

    /**
     * Always emits the latest dashboard. Automatically updated when [requestReloadDashboard] is requested,
     * the login status changes or the server is updated.
     */
    private fun loadDashboard(context: CoroutineContext) {
        viewModelScope.launch(context) {
            val authToken = accountService.authToken.first()
            val serverUrl = serverConfigurationService.serverUrl.first()
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                dashboardService.loadDashboard(authToken, serverUrl).bind { dashboard ->
                    extractCoursesInSections(serverUrl, dashboard)
                }
            }.collect {
                _dashboardState.value = it
            }
        }
    }

    /**
     * Request a reload of the dashboard.
     */
    fun requestReloadDashboard() {
        reloadDashboard.tryEmit(Unit)
    }

    fun onUpdateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun onUpdateSorting(newSorting: CourseSorting) {
        sorting.value = newSorting
    }

    suspend fun onCourseAccessed(courseId: Long) {
        val serverUrl = serverConfigurationService.serverUrl.first()
        dashboardStorageService.onCourseAccessed(
            courseId = courseId,
            serverHost = serverUrl
        )
        courseAccess.tryEmit(courseId)
    }

    /**
     * Checks for recently accessed courses and adds them to the recent courses section.
     */
    private suspend fun extractCoursesInSections(
        serverUrl: String,
        currentDashboard: Dashboard,
        sorting: CourseSorting = CourseSorting.ALPHABETICAL_ASCENDING
    ): Dashboard {
        if (currentDashboard.courses.size <= 5) {
            return currentDashboard
        }

        val recentlyAccessedCourseMap =
            dashboardStorageService.getLastAccesssedCourses(serverUrl).first()

        val newlyRecentCourses = currentDashboard.courses.filter { course ->
            recentlyAccessedCourseMap.containsKey(course.course.id)
        }.toSet()
        val noLongerRecentCourses = currentDashboard.recentCourses.filter { course ->
            !recentlyAccessedCourseMap.containsKey(course.course.id)
        }.toSet()

        if (newlyRecentCourses.isEmpty() && noLongerRecentCourses.isEmpty()) {
            return currentDashboard
        }

        val updatedCourses = currentDashboard.courses - newlyRecentCourses + noLongerRecentCourses
        val updatedRecentCourses = currentDashboard.recentCourses - noLongerRecentCourses + newlyRecentCourses

       return Dashboard(
            courses = sortCourses(updatedCourses, sorting),
            recentCourses = sortCourses(updatedRecentCourses, sorting)
        )
    }

    private fun sortCourses(
        courses: List<CourseWithScore>,
        sorting: CourseSorting
    ): MutableList<CourseWithScore> {
        return if (sorting == CourseSorting.ALPHABETICAL_ASCENDING) {
            courses.sortedBy { it.course.title }.toMutableList()
        } else {
            courses.sortedByDescending { it.course.title }.toMutableList()
        }
    }
}

enum class CourseSorting {
    ALPHABETICAL_ASCENDING,
    ALPHABETICAL_DESCENDING,
}