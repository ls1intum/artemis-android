package de.tum.informatics.www1.artemis.native_app.core.data.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.service.ArtemisContextBasedService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise

interface ExerciseService : ArtemisContextBasedService {

    /**
     * Get exercise details including all results for the currently logged-in user
     * @param exerciseId - Id of the exercise to get the repos from
     */
    suspend fun getExerciseDetails(
        exerciseId: Long,
    ): NetworkResponse<Exercise>
}
