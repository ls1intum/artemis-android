package de.tum.informatics.www1.artemis.native_app.feature.quiz.service

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.QuizSubmission
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Result
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.Submission

interface QuizParticipationService {

    suspend fun submitForPractice(
        submission: QuizSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Result>

    /**
     * Saves the answers of a live quiz without submitting them, so they can still be changed.
     */
    suspend fun saveForLiveMode(
        submission: QuizSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Submission>

    /**
     * Submits the answers of a live quiz. They cannot be changed afterwards.
     */
    suspend fun submitForLiveMode(
        submission: QuizSubmission,
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Submission>
}