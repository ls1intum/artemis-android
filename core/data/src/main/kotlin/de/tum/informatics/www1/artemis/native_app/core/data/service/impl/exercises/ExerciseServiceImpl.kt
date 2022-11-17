package de.tum.informatics.www1.artemis.native_app.core.data.service.impl.exercises

import de.tum.informatics.www1.artemis.native_app.android.model.exercise.Exercise
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

internal class ExerciseServiceImpl(private val ktorProvider: KtorProvider) :
    de.tum.informatics.www1.artemis.native_app.core.data.service.ExerciseService {
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