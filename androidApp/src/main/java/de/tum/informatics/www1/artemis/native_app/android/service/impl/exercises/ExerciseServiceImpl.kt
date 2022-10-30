package de.tum.informatics.www1.artemis.native_app.android.service.impl.exercises

import de.tum.informatics.www1.artemis.native_app.android.content.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.android.service.exercises.ExerciseService
import de.tum.informatics.www1.artemis.native_app.android.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.android.util.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.android.util.performNetworkCall
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ExerciseServiceImpl(private val ktorProvider: KtorProvider) : ExerciseService {
    override suspend fun getExerciseDetails(
        exerciseId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Exercise> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "exercises", exerciseId.toString(), "details")
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
            }.body()
        }
    }
}