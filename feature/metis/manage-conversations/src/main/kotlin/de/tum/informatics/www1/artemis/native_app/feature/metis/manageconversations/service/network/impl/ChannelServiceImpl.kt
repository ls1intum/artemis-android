package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
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

class ChannelServiceImpl(
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
) : ArtemisContextBasedServiceImpl(ktorProvider, artemisContextProvider), ChannelService {

    override suspend fun getChannels(courseId: Long): NetworkResponse<List<ChannelChat>> {
        return getRequest {
            url {
                appendPathSegments(
                    "api",
                    "courses",
                    courseId.toString(),
                    "channels",
                    "overview"
                )
            }
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
        courseId: Long
    ): NetworkResponse<ChannelChat> {
        return getRequest {
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
        }
    }

    override suspend fun registerInChannel(
        courseId: Long,
        conversationId: Long,
    ): NetworkResponse<Boolean> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl()) {
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

                setBody(listOf(artemisContext().username))
                contentType(ContentType.Application.Json)

                cookieAuth(authToken())
            }
                .status
                .isSuccess()
        }
    }
}