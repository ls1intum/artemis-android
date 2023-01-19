package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.CourseExerciseService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class CourseExerciseServiceImpl(
    private val ktorProvider: KtorProvider
) : CourseExerciseService {

    override suspend fun startExercise(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Participation> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "exercises", exerciseId.toString(), "participations")
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
            }.body()
        }
    }
}