package de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ChannelService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class ChannelServiceImpl(private val ktorProvider: KtorProvider) : ChannelService {

    override suspend fun getChannels(
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<ChannelChat>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "channels",
                        "overview"
                    )
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }.body()
        }
    }

    override suspend fun registerInChannel(
        courseId: Long,
        conversationId: Long,
        username: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Boolean> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "channels",
                        conversationId.toString(),
                        "register"
                    )
                }

                setBody(listOf(username))
                contentType(ContentType.Application.Json)

                cookieAuth(authToken)
            }
                .status
                .isSuccess()
        }
    }
}