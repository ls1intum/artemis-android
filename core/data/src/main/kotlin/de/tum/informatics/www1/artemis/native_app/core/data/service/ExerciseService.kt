package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface ExerciseService {

    /**
     * Get exercise details including all results for the currently logged-in user
     * @param exerciseId - Id of the exercise to get the repos from
     */
    fun getExerciseDetails(exerciseId: Int, serverUrl: String, authToken: String): Flow<DataState<Exercise>>

    /**
     * Reqzest the latest due date.
     */
    fun getLatestDueDate(exerciseId: Int, serverUrl: String, authToken: String): Flow<DataState<Instant?>>
}