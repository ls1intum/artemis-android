package de.tum.informatics.www1.artemis.native_app.core.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * Shares common exercise starting logic.
 */
abstract class BaseExerciseListViewModel(
    private val courseExerciseService: CourseExerciseService
) : ViewModel() {

    protected companion object {
        fun Flow<Result>.mapFilterToNewParticipationData(): Flow<NewParticipationData> = mapNotNull {
            NewParticipationData(
                exerciseId = it.participation?.exercise?.id
                    ?: return@mapNotNull null,
                newParticipation = it.participation
                    ?: return@mapNotNull null
            )
        }
    }

    /**
     * Emitted to when the user has started an exercise.
     */
    protected val startedExerciseFlow = MutableSharedFlow<NewParticipationData>()

    fun startExercise(exerciseId: Long, onStartedSuccessfully: (participationId: Long) -> Unit) {
        viewModelScope.launch {
            when (val response = courseExerciseService.startExercise(exerciseId)) {
                is NetworkResponse.Response -> {
                    startedExerciseFlow.emit(NewParticipationData(exerciseId, response.data))

                    onStartedSuccessfully(
                        response.data.id ?: return@launch
                    )
                }
                is NetworkResponse.Failure -> {}
            }
        }
    }

    protected data class NewParticipationData(
        val exerciseId: Long,
        val newParticipation: Participation
    )
}