package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.ParticipationService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

internal class ParticipationServiceImpl(private val ktorProvider: KtorProvider) : ParticipationService {
    override suspend fun findParticipation(
        exerciseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Participation> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "exercises", exerciseId.toString(), "participation")
                }

                contentType(ContentType.Application.Json)
                bearerAuth(authToken)
            }.body()
        }
    }
}