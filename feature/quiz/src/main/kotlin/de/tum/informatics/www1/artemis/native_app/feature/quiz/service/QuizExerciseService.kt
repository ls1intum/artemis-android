package de.tum.informatics.www1.artemis.native_app.feature.quiz.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.QuizExercise

internal interface QuizExerciseService {

    /**
     * Find the quiz exercise with the given id, with information filtered for students
     * @param exerciseId the id of the quiz exercise that should be loaded
     */
    suspend fun findForStudent(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise>

    suspend fun join(
        exerciseId: Long,
        password: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<QuizExercise.QuizBatch>
}