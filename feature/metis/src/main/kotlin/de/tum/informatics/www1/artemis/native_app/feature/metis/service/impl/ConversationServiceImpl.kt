package de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.Serializable

class ConversationServiceImpl(private val ktorProvider: KtorProvider) : ConversationService {

    override suspend fun getConversations(
        courseId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<Conversation>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "conversations")
                }

                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun searchForPotentialCommunicationParticipants(
        courseId: Long,
        query: String,
        includeStudents: Boolean,
        includeTutors: Boolean,
        includeInstructors: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<User>> {
        val roles: List<String> =
            (if (includeStudents) listOf("students") else emptyList()) +
                    (if (includeInstructors) listOf("instructors") else emptyList()) +
                    (if (includeTutors) listOf("tutors") else emptyList())

        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "users", "search")

                    parameter("loginOrName", query)
                    parameter("roles", roles.joinToString(","))
                }

                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun createGroupChat(
        courseId: Long,
        groupMembers: List<String>,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<GroupChat> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "group-chats")
                }

                setBody(groupMembers)
                contentType(ContentType.Application.Json)

                accept(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun createOneToOneConversation(
        courseId: Long,
        partner: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<OneToOneChat> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "one-to-one-chats")
                }

                setBody(listOf(partner))
                contentType(ContentType.Application.Json)

                accept(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    override suspend fun createChannel(
        courseId: Long,
        name: String,
        description: String,
        isPublic: Boolean,
        isAnnouncement: Boolean,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<ChannelChat> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString(), "channels")
                }

                setBody(
                    CreateChannelData(
                        isPublic = isPublic,
                        isAnnouncementChannel = isAnnouncement,
                        name = name
                    )
                )
                contentType(ContentType.Application.Json)

                accept(ContentType.Application.Json)
                cookieAuth(authToken)
            }.body()
        }
    }

    @Serializable
    private data class CreateChannelData(
        val type: String = "channel",
        val isPublic: Boolean,
        val isAnnouncementChannel: Boolean,
        val name: String
    )

    override suspend fun updateConversation(
        courseId: Long,
        conversationId: Long,
        newName: String?,
        newDescription: String?,
        newTopic: String?,
        conversation: Conversation,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString())

                    appendConversationTypeWithId(conversation, conversationId)
                }

                setBody(
                    UpdateConversationData(
                        type = conversation.typeAsString,
                        name = newName,
                        description = newDescription,
                        topic = newTopic
                    )
                )

                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                cookieAuth(authToken)
            }
                .status
                .isSuccess()
        }
    }

    @Serializable
    private data class UpdateConversationData(
        val type: String,
        val name: String?,
        val description: String?,
        val topic: String?
    )

    override suspend fun archiveChannel(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> {
        return performActionOnChannel(
            courseId,
            conversationId,
            authToken,
            serverUrl
        ) {
            appendPathSegments("archive")
        }
    }

    override suspend fun unarchiveChannel(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> {
        return performActionOnChannel(
            courseId,
            conversationId,
            authToken,
            serverUrl
        ) {
            appendPathSegments("unarchive")
        }
    }

    override suspend fun getMembers(
        courseId: Long,
        conversationId: Long,
        query: String,
        size: Int,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<ConversationUser>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        "conversations",
                        conversationId.toString(),
                        "members",
                        "search"
                    )

                    parameter("page", pageNum)
                    parameter("size", size)
                    parameter("loginOrName", query)
                }

                cookieAuth(authToken)
                contentType(ContentType.Application.Json)
            }
                .body()
        }
    }

    override suspend fun kickMember(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> {
        return performActionOnUser(
            courseId = courseId,
            conversation = conversation,
            user = user,
            authToken = authToken,
            serverUrl = serverUrl
        ) {
            appendPathSegments("deregister")
        }
    }

    override suspend fun registerMembers(
        courseId: Long,
        conversation: Conversation,
        users: List<String>,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> {
        return performActionOnUsers(
            courseId,
            conversation,
            users,
            authToken,
            serverUrl
        ) {
            appendPathSegments("register")
        }
    }

    override suspend fun grantModerationRights(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> {
        return performActionOnUser(
            courseId = courseId,
            conversation = conversation,
            user = user,
            authToken = authToken,
            serverUrl = serverUrl
        ) {
            appendPathSegments("grant-channel-moderator")
        }
    }

    override suspend fun revokeModerationRights(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<Boolean> {
        return performActionOnUser(
            courseId = courseId,
            conversation = conversation,
            user = user,
            authToken = authToken,
            serverUrl = serverUrl
        ) {
            appendPathSegments("revoke-channel-moderator")
        }
    }

    override suspend fun markConversationAsHidden(
        courseId: Long,
        conversationId: Long,
        hidden: Boolean,
        authToken: String,
        serverUrl: String
    ) = performActionOnConversation(
        courseId,
        conversationId,
        authToken = authToken,
        serverUrl = serverUrl,
        httpRequestBlock = {
            parameter("isHidden", hidden)
        }
    ) {
        appendPathSegments("hidden")
    }

    override suspend fun markConversationAsFavorite(
        courseId: Long,
        conversationId: Long,
        favorite: Boolean,
        authToken: String,
        serverUrl: String
    ) = performActionOnConversation(
        courseId,
        conversationId,
        authToken = authToken,
        serverUrl = serverUrl,
        httpRequestBlock = {
            parameter("isFavorite", favorite)
        }
    ) {
        appendPathSegments("favorite")
    }

    private suspend fun performActionOnUser(
        courseId: Long,
        conversation: Conversation,
        user: String,
        authToken: String,
        serverUrl: String,
        urlBlock: URLBuilder.() -> Unit
    ): NetworkResponse<Boolean> =
        performActionOnUsers(courseId, conversation, listOf(user), authToken, serverUrl, urlBlock)

    private suspend fun performActionOnUsers(
        courseId: Long,
        conversation: Conversation,
        users: List<String>,
        authToken: String,
        serverUrl: String,
        urlBlock: URLBuilder.() -> Unit
    ): NetworkResponse<Boolean> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments("api", "courses", courseId.toString())

                    appendConversationTypeWithId(conversation, conversation.id)

                    urlBlock()
                }

                setBody(users)

                cookieAuth(authToken)
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }
                .status
                .isSuccess()
        }
    }

    private suspend fun performActionOnChannel(
        courseId: Long,
        conversationId: Long,
        authToken: String,
        serverUrl: String,
        urlBlock: URLBuilder.() -> Unit
    ): NetworkResponse<Boolean> = performActionOnConversation(
        courseId,
        conversationId,
        "channels",
        authToken,
        serverUrl,
        urlBlock = urlBlock
    )

    private suspend fun performActionOnConversation(
        courseId: Long,
        conversationId: Long,
        conversationTypePath: String = "conversations",
        authToken: String,
        serverUrl: String,
        httpRequestBlock: HttpRequestBuilder.() -> Unit = {},
        urlBlock: URLBuilder.() -> Unit
    ): NetworkResponse<Boolean> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(
                        "api",
                        "courses",
                        courseId.toString(),
                        conversationTypePath,
                        conversationId.toString()
                    )

                    urlBlock()
                }

                httpRequestBlock()

                cookieAuth(authToken)
            }
                .status
                .isSuccess()
        }
    }

    private fun URLBuilder.appendConversationTypeWithId(
        conversation: Conversation,
        conversationId: Long
    ) {
        when (conversation) {
            is ChannelChat -> appendPathSegments("channels", conversationId.toString())
            is GroupChat -> appendPathSegments("group-chats", conversationId.toString())
            is OneToOneChat -> {} // One to one chats cannot be updated
        }
    }
}
