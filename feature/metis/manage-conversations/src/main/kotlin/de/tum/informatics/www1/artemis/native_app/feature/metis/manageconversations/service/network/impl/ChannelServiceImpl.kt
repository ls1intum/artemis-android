package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.ArtemisContextBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
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

                setBody(artemisContext().username)
                contentType(ContentType.Application.Json)

                cookieAuth(authToken())
            }
                .status
                .isSuccess()
        }
    }
}