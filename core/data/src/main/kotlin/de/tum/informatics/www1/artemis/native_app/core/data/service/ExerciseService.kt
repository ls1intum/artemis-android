package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import kotlinx.datetime.Instant

interface ExerciseService {

    /**
     * Get exercise details including all results for the currently logged-in user
     * @param exerciseId - Id of the exercise to get the repos from
     */
    suspend fun getExerciseDetails(exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<Exercise>

    /**
     * Reqzest the latest due date.
     */
    suspend fun getLatestDueDate(exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<Instant?>
}