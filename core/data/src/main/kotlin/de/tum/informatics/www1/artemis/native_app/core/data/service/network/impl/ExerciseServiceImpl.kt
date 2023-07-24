package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ExerciseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.Exercise
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class ExerciseServiceImpl(
    private val ktorProvider: KtorProvider,
    private val jsonProvider: JsonProvider
) : ExerciseService {
    override suspend fun getExerciseDetails(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Exercise> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "exercises", exerciseId.toString(), "details")
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }
}