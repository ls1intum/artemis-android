package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.BuildLogService
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.exercise.submission.BuildLogEntry
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow

internal class BuildLogServiceImpl(
    private val networkStatusProvider: NetworkStatusProvider,
    private val ktorProvider: KtorProvider
) : BuildLogService {

    override fun loadBuildLogs(
        participationId: Long,
        resultId: Long?,
        serverUrl: String,
        bearerToken: String
    ): Flow<DataState<List<BuildLogEntry>>> {
        return retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            performNetworkCall {
                ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments(
                            "api",
                            "repository",
                            participationId.toString(),
                            "buildlogs"
                        )
                    }
                    if (resultId != null) {
                        parameter("resultId", resultId)
                    }

                    contentType(ContentType.Application.Json)
                    cookieAuth(bearerToken)
                }.body()
            }
        }
    }
}