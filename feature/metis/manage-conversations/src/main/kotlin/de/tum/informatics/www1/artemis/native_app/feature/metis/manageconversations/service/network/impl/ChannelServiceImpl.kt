package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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

    override suspend fun getUnresolvedChannels(
        courseId: Long,
        channelIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<Conversation>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "messages"
                    )
                }

                parameter("courseWideChannelIds", channelIds.joinToString(","))
                parameter("postSortCriterion", "CREATION_DATE")
                parameter("sortingOrder", "DESCENDING")
                parameter("pagingEnabled", "true")
                parameter("page", "0")
                parameter("size", "\\(50)")
                parameter("filterToUnresolved", "true")

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }.body() as List<StandalonePost>
        }.bind {
            it.map { message ->
                message.conversation ?: error("Conversation is null")
            }.distinct()
        }
    }

    override suspend fun getExerciseChannel(
        exerciseId: Long,
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<ChannelChat> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "exercises",
                        exerciseId.toString(),
                        "channel"
                    )
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }.body()
        }
    }

    override suspend fun getLectureChannel(
        lectureId: Long,
        courseId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<ChannelChat> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "lectures",
                        lectureId.toString(),
                        "channel"
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