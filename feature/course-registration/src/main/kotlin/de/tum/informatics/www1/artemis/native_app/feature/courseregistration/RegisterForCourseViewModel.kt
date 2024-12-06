package de.tum.informatics.www1.artemis.native_app.feature.courseregistration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.authTokenStateFlow
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.courseregistration.service.CourseRegistrationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class RegisterForCourseViewModel(
    accountService: AccountService,
    private val courseRegistrationService: CourseRegistrationService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val reloadRegistrableCourses = MutableSharedFlow<Unit>()

    val registrableCourses: StateFlow<DataState<List<SemesterCourses>>> = flatMapLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        reloadRegistrableCourses.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            courseRegistrationService.fetchRegistrableCourses(
                serverUrl,
                authToken
            )
        }
    }
        .distinctUntilChanged() //No need to perform expensive group operation if not changed.
        .map { dataState ->
            dataState.bind { courses ->
                courses
                    .groupBy { it.semester }
                    .map { (semester, courses) -> SemesterCourses(semester, courses) }
            }
        }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, initialValue = DataState.Loading())

    private val authToken: StateFlow<String> = authTokenStateFlow(accountService)
    private val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    fun reloadRegistrableCourses() {
        viewModelScope.launch(coroutineContext) {
            reloadRegistrableCourses.emit(Unit)
        }
    }

    /**
     * Deferred contains the course id on success, null otherwise
     */
    fun registerInCourse(course: Course): Deferred<Long?> {
        return viewModelScope.async(coroutineContext) {
            val courseId = course.id ?: 0L
            courseRegistrationService.registerInCourse(
                serverUrl.first(),
                authToken.first(),
                courseId
            )
                .bind { course.id }
                .orNull()
        }
    }

    data class SemesterCourses(val semester: String, val courses: List<Course>)
}