package de.tum.informatics.www1.artemis.native_app.feature.force_update.service


import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType

interface UpdateService {
    suspend fun getLatestVersion(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Int>
    //suspend fun getLatestVersion(): NetworkResponse<Int>
}

class UpdateServiceImpl(
    private val ktorProvider: KtorProvider
) : UpdateService {

    override suspend fun getLatestVersion(
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Int> {
        return performNetworkCall {
            val dto: AndroidVersionDto = ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "versions", "android")
                }
                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()

            dto.min.toInt()
        }
    }

//    override suspend fun getLatestVersion(): NetworkResponse<Int> {
//
//        return NetworkResponse.Response(Random.nextInt(615, 626))
//    }

}




