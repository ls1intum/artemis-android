package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.model.server_config.ProfileInfo
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

internal class ServerDataServiceImpl(private val ktorProvider: KtorProvider) : ServerDataService {

    override suspend fun getServerProfileInfo(serverUrl: String): NetworkResponse<ProfileInfo> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url { appendPathSegments("management", "info") }

                accept(ContentType.Application.Json)
            }.body()
        }
    }

    override suspend fun getAccountData(
        serverUrl: String,
        bearerToken: String
    ): NetworkResponse<Account> {
        return performNetworkCall {
            ktorProvider
                .ktorClient
                .get(serverUrl) {
                    url {
                        appendPathSegments("api", "account")
                    }

                    contentType(ContentType.Application.Json)
                    bearerAuth(bearerToken)
                }.body()
        }
    }
}