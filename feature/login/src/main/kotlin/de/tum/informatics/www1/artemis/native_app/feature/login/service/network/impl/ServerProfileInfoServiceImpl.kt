package de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.ServerProfileInfoService
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments

internal class ServerProfileInfoServiceImpl(private val ktorProvider: KtorProvider) :
    ServerProfileInfoService {
    override suspend fun getServerProfileInfo(serverUrl: String): NetworkResponse<ProfileInfo> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url { appendPathSegments("management", "info") }

                accept(ContentType.Application.Json)
            }.body()
        }
    }
}
