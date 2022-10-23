package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course_registation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.content.Course
import de.tum.informatics.www1.artemis.native_app.android.service.AccountService
import de.tum.informatics.www1.artemis.native_app.android.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.android.service.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.android.service.ServerCommunicationProvider
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.retryOnInternet
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterForCourseViewModel(
    private val accountService: AccountService,
    private val courseRegistrationService: CourseRegistrationService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverCommunicationProvider: ServerCommunicationProvider
) : ViewModel() {

    private val reloadRegistrableCourses = MutableSharedFlow<Unit>()

    val registrableCourses: Flow<DataState<List<SemesterCourses>>> = combine(
        accountService.authenticationData,
        serverCommunicationProvider.serverUrl,
        reloadRegistrableCourses.onStart { emit(Unit) }
    ) { a, b, _ -> a to b }
        .transformLatest { (authData, serverUrl) ->
            when (authData) {
                is AccountService.AuthenticationData.LoggedIn -> {
                    emitAll(
                        retryOnInternet(
                            networkStatusProvider.currentNetworkStatus
                        ) {
                            courseRegistrationService.fetchRegistrableCourses(
                                serverUrl,
                                authData.authToken
                            )
                        }
                    )
                }
                AccountService.AuthenticationData.NotLoggedIn -> emit(DataState.Suspended())
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

    fun reloadRegistrableCourses() {
        viewModelScope.launch {
            reloadRegistrableCourses.emit(Unit)
        }
    }

    fun registerInCourse(course: Course, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            val authToken = when(val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                AccountService.AuthenticationData.NotLoggedIn -> {
                    onFailure()
                    return@launch
                }
            }

            courseRegistrationService.registerInCourse(serverCommunicationProvider.serverUrl.first(), authToken, course.id)
            onSuccess()
        }
    }

    data class SemesterCourses(val semester: String, val courses: List<Course>)
}