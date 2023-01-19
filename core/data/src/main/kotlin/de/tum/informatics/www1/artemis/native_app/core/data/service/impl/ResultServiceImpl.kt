package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ResultService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.feedback.Feedback
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow

internal class ResultServiceImpl(
    private val networkStatusProvider: NetworkStatusProvider,
    private val ktorProvider: KtorProvider
) : ResultService {
    override fun getFeedbackDetailsForResult(
        participationId: Long,
        resultId: Long,
        serverUrl: String,
        bearerToken: String
    ): Flow<DataState<List<Feedback>>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            performNetworkCall {
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
                    bearerAuth(bearerToken)
                }.body()
            }
        }
    }
}