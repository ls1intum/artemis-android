package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Abstract view model such that one can display the exercise action buttons
 */
abstract class BaseExerciseViewModel(
    private val exerciseId: Long,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val courseExerciseService: CourseExerciseService
) : ViewModel() {

    abstract val exercise: Flow<DataState<Exercise>>

    /**
     * Emitted to when startExercise is successful
     */
    private val _gradedParticipation = MutableSharedFlow<Participation>()

    // Wrap in flow to avoid "Accessing non-final property in constructor"
    val gradedParticipation: StateFlow<Participation?> = flow {
        emitAll(
            merge<Participation?>(
                _gradedParticipation,
                exercise
                    .filterSuccess()
                    .map { exercise ->
                        exercise
                            .studentParticipations
                            .orEmpty()
                            .filterIsInstance<StudentParticipation>()
                            .firstOrNull()
                    }
                    .filterNotNull()
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun startExercise(onStartedSuccessfully: (participationId: Long) -> Unit) {
        viewModelScope.launch {
            val serverUrl = serverConfigurationService.serverUrl.first()
            when (val authData = accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> {
                    val response = courseExerciseService.startExercise(
                        exerciseId,
                        serverUrl,
                        authData.authToken
                    )

                    when (response) {
                        is NetworkResponse.Response -> {
                            val participation = response.data

                            _gradedParticipation.emit(participation)
                            onStartedSuccessfully(participation.id ?: return@launch)
                        }
                        is NetworkResponse.Failure -> {}
                    }
                }

                AccountService.AuthenticationData.NotLoggedIn -> {}
            }
        }
    }
}