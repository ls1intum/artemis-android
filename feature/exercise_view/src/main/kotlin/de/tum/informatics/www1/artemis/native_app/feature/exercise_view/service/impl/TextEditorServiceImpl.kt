package de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.participation.Participation
import de.tum.informatics.www1.artemis.native_app.feature.exercise_view.service.TextEditorService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

class TextEditorServiceImpl(private val ktorProvider: KtorProvider) : TextEditorService {

    override suspend fun getParticipation(
        participationId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Participation> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "text-editor", participationId.toString())
                }

                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }
}