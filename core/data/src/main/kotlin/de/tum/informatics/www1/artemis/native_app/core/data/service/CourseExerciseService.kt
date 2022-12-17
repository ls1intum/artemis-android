package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.StudentParticipation

interface CourseExerciseService {

    suspend fun startExercise(exerciseId: Long, serverUrl: String, authToken: String): NetworkResponse<Participation>
}