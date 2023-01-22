package de.tum.informatics.www1.artemis.native_app.feature.course_registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.transformLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseRegistrationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.ui.authenticationStateFlow
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class RegisterForCourseViewModel(
    private val accountService: AccountService,
    private val courseRegistrationService: CourseRegistrationService,
    private val serverConfigurationService: ServerConfigurationService
) : ViewModel() {

    private val reloadRegistrableCourses = MutableSharedFlow<Unit>()

    val registrableCourses: StateFlow<DataState<List<SemesterCourses>>> = transformLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        reloadRegistrableCourses.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        emitAll(
            courseRegistrationService.fetchRegistrableCourses(
                serverUrl,
                authToken
            )
        )
    }
        .distinctUntilChanged() //No need to perform expensive group operation if not changed.
        .map { dataState ->
            dataState.bind { courses ->
                courses
                    .groupBy { it.semester }
                    .map { (semester, courses) -> SemesterCourses(semester, courses) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = DataState.Loading())

    val authenticationData: StateFlow<AccountService.AuthenticationData> = authenticationStateFlow(accountService)
    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    fun reloadRegistrableCourses() {
        viewModelScope.launch {
            reloadRegistrableCourses.emit(Unit)
        }
    }

    fun registerInCourse(course: Course, onSuccess: () -> Unit, onFailure: () -> Unit) {
        viewModelScope.launch {
            val authToken = when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> authData.authToken
                AccountService.AuthenticationData.NotLoggedIn -> {
                    onFailure()
                    return@launch
                }
            }

            courseRegistrationService.registerInCourse(
                serverConfigurationService.serverUrl.first(),
                authToken,
                course.id
            )
            onSuccess()
        }
    }

    data class SemesterCourses(val semester: String, val courses: List<Course>)
}