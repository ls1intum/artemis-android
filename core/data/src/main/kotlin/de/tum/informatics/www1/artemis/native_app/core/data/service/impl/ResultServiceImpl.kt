package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.ResultService
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback.Feedback
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class ResultServiceImpl(
    private val ktorProvider: KtorProvider
) : ResultService {
    override suspend fun getFeedbackDetailsForResult(
        participationId: Long,
        resultId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<Feedback>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "participations",
                        participationId.toString(),
                        "results",
                        resultId.toString(),
                        "details"
                    )
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }
}