package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.CourseBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.network.ChannelService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
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
) : CourseBasedServiceImpl(ktorProvider, artemisContextProvider), ChannelService {

    override suspend fun getChannels(): NetworkResponse<List<ChannelChat>> {
        val courseId = courseId()
        return getRequest {
            url {
                appendPathSegments(
                    *Api.Communication.Courses.path,
                    courseId.toString(),
                    "channels",
                    "overview"
                )
            }
        }
    }

    override suspend fun getUnresolvedChannels(
        channelIds: List<Long>,
    ): NetworkResponse<List<Conversation>> {
        val courseId = courseId()
        return getRequest<List<StandalonePost>> {
            url {
                appendPathSegments(
                    *Api.Communication.Courses.path,
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
        }.bind {
            it.map { message ->
                message.conversation ?: error("Conversation is null")
            }.distinct()
        }
    }

    override suspend fun getExerciseChannel(
        exerciseId: Long,
    ): NetworkResponse<ChannelChat> {
        val courseId = courseId()
        return getRequest {
            url {
                appendPathSegments(
                    *Api.Communication.Courses.path,
                    courseId.toString(),
                    "exercises",
                    exerciseId.toString(),
                    "channel"
                )
            }
        }
    }

    override suspend fun getLectureChannel(
        lectureId: Long,
    ): NetworkResponse<ChannelChat> {
        val courseId = courseId()
        return getRequest {
            url {
                appendPathSegments(
                    *Api.Communication.Courses.path,
                    courseId.toString(),
                    "lectures",
                    lectureId.toString(),
                    "channel"
                )
            }
        }
    }

    override suspend fun registerInChannel(
        conversationId: Long,
    ): NetworkResponse<Boolean> {
        val artemisContext = artemisContext()
        val courseId = artemisContext.courseId

        return performNetworkCall {
            ktorProvider.ktorClient.post(artemisContext.serverUrl) {
                url {
                    appendPathSegments(
                        *Api.Communication.Courses.path,
                        courseId.toString(),
                        "channels",
                        conversationId.toString(),
                        "register"
                    )
                }

                setBody(listOf(artemisContext.loginName))
                contentType(ContentType.Application.Json)

                cookieAuth(artemisContext.authToken)
            }
                .status
                .isSuccess()
        }
    }
}