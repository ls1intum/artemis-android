package de.tum.informatics.www1.artemis.native_app.android.service.exercises

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse

interface ExerciseService {

    /**
     * Get exercise details including all results for the currently logged-in user
     * @param exerciseId - Id of the exercise to get the repos from
     */
    suspend fun getExerciseDetails(exerciseId: Int, serverUrl: String, authToken: String): NetworkResponse<Exercise>
}