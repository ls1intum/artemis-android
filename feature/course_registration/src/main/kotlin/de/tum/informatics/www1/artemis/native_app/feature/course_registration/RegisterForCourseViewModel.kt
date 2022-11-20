package de.tum.informatics.www1.artemis.native_app.feature.course_registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterForCourseViewModel(
    private val accountService: AccountService,
    private val courseRegistrationService: CourseRegistrationService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService
) : ViewModel() {

    private val reloadRegistrableCourses = MutableSharedFlow<Unit>()

    val registrableCourses: Flow<DataState<List<SemesterCourses>>> = combine(
        accountService.authenticationData,
        serverConfigurationService.serverUrl,
        reloadRegistrableCourses.onStart { emit(Unit) }
    ) { a, b, _ -> a to b }
        .transformLatest { (authData, serverUrl) ->
            when (authData) {
                is AccountService.AuthenticationData.LoggedIn -> {
                    emitAll(
                        courseRegistrationService.fetchRegistrableCourses(
                            serverUrl,
                            authData.authToken
                        )
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

            courseRegistrationService.registerInCourse(serverConfigurationService.serverUrl.first(), authToken, course.id)
            onSuccess()
        }
    }

    data class SemesterCourses(val semester: String, val courses: List<Course>)
}