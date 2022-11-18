package de.tum.informatics.www1.artemis.native_app.feature.exercise_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.android.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.websocket.ParticipationService
import kotlinx.coroutines.flow.*

internal class ExerciseViewModel(
    private val exerciseId: Int,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val exerciseService: ExerciseService,
    private val participationService: ParticipationService
) : ViewModel() {

    private val requestReloadExercise = MutableSharedFlow<Unit>()

    private val fetchedExercise: Flow<DataState<Exercise>> =
        combine(
            serverConfigurationService.serverUrl,
            accountService.authenticationData,
            requestReloadExercise.onStart { emit(Unit) }
        ) { serverUrl, authData, _ ->
            serverUrl to authData
        }
            .transformLatest { (serverUrl, authData) ->
                when (authData) {
                    is AccountService.AuthenticationData.LoggedIn -> {
                        emitAll(
                            exerciseService.getExerciseDetails(
                                exerciseId,
                                serverUrl,
                                authData.authToken
                            )
                        )
                    }
                    AccountService.AuthenticationData.NotLoggedIn -> {
                        emit(DataState.Suspended())
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, DataState.Loading())

    /**
     * Emits the exercise updated with the latest participations and results
     */
    val exercise: Flow<DataState<Exercise>> =
        fetchedExercise
            .transformLatest { exercise ->
                when (exercise) {
                    is DataState.Success -> {
                        var currentExercise = exercise.data

                        emitAll(
                            participationService
                                .personalSubmissionUpdater.filter { result -> result.participation?.exercise?.id == exerciseId }
                                .transformLatest inner@{ result ->
                                    val participation = result.participation ?: return@inner

                                    currentExercise =
                                        currentExercise.withUpdatedParticipation(participation)

                                    emit(DataState.Success(currentExercise))
                                }
                        )
                    }
                    else -> emit(exercise)
                }
            }

    fun requestReloadExercise() {
        requestReloadExercise.tryEmit(Unit)
    }
}