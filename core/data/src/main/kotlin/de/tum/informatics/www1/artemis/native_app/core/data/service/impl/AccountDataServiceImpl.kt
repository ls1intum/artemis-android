package de.tum.informatics.www1.artemis.native_app.core.data.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

internal class AccountDataServiceImpl(private val ktorProvider: KtorProvider) : AccountDataService {

    override suspend fun getAccountData(
        serverUrl: String,
        bearerToken: String
    ): NetworkResponse<Account> {
        return performNetworkCall {
            ktorProvider
                .ktorClient
                .get(serverUrl) {
                    url {
                        appendPathSegments("api", "public", "account")
                    }

                    contentType(ContentType.Application.Json)
                    cookieAuth(bearerToken)
                }.body()
        }
    }
}
