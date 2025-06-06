package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.ForwardedMessage
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class MetisServiceImpl(
    private val ktorProvider: KtorProvider,
) : MetisService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    override suspend fun getPosts(
        standalonePostsContext: MetisService.StandalonePostsContext,
        pageSize: Int,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<StandalonePost>> {
        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                val metisContext = standalonePostsContext.metisContext
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(metisContext.courseId.toString())
                    appendPathSegments(Api.Communication.standalonePostSegment)
                }

                when (standalonePostsContext.sortingStrategy) {
                    MetisSortingStrategy.DATE_ASCENDING, MetisSortingStrategy.DATE_DESCENDING -> {
                        parameter("postSortCriterion", "CREATION_DATE")
                    }

                    MetisSortingStrategy.REPLIES_ASCENDING, MetisSortingStrategy.REPLIES_DESCENDING -> {
                        parameter("postSortCriterion", "ANSWER_COUNT")
                    }

                    MetisSortingStrategy.VOTES_ASCENDING, MetisSortingStrategy.VOTES_DESCENDING -> {
                        parameter("postSortCriterion", "ANSWER_COUNT")
                    }
                }

                parameter(
                    "sortingOrder",
                    standalonePostsContext.sortingStrategy.httpParamValue
                )

                when (metisContext) {
                    is MetisContext.Conversation -> {
                        parameter("conversationIds", metisContext.conversationId)
                    }

                    is MetisContext.Course -> {

                    }
                }

                if (standalonePostsContext.courseWideContext != null) {
                    parameter(
                        "courseWideContext",
                        standalonePostsContext.courseWideContext.httpValue
                    )
                }

                if (standalonePostsContext.query != null) {
                    parameter("searchText", standalonePostsContext.query)
                }

                if (standalonePostsContext.courseWideContext != CourseWideContext.ANNOUNCEMENT) {
                    parameter(
                        "filterToUnresolved",
                        standalonePostsContext.filter is MetisFilter.Unresolved
                    )
                    if (standalonePostsContext.filter is MetisFilter.CreatedByClient) {
                        parameter(
                            "authorIds",
                            standalonePostsContext.filter.userId
                        )
                    }
                    if (standalonePostsContext.filter is MetisFilter.CreatedByAuthors) {
                        parameter(
                            "authorIds",
                            standalonePostsContext.filter.userIds.joinToString(",")
                        )
                    }
                    parameter(
                        "filterToAnsweredOrReacted",
                         standalonePostsContext.filter is MetisFilter.WithReaction
                    )
                    parameter(
                        "pinnedOnly",
                        standalonePostsContext.filter is MetisFilter.Pinned
                    )
                }

                parameter("pagingEnabled", true)
                parameter("page", pageNum)
                parameter("pageSize", pageSize)

                cookieAuth(authToken)
            }.body()
        }
    }

    /**
     * Uses the fact that you can query a single post using the query parameter.
     * Therefore, no extra API is required.
     */
    override suspend fun getPost(
        metisContext: MetisContext,
        serverSidePostId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        val posts = getPosts(
            standalonePostsContext = MetisService.StandalonePostsContext(
                metisContext = metisContext,
                filter = MetisFilter.All,
                query = "#$serverSidePostId",
                sortingStrategy = MetisSortingStrategy.DATE_DESCENDING,
                courseWideContext = null
            ),
            pageSize = 20,
            pageNum = 0,
            authToken = authToken,
            serverUrl = serverUrl
        )

        when (posts) {
            is NetworkResponse.Failure -> return NetworkResponse.Failure(posts.exception)
            is NetworkResponse.Response -> {
                if (posts.data.size != 1) {
                    return NetworkResponse.Failure(RuntimeException("Expected exactly one post"))
                }

                return NetworkResponse.Response(posts.data.first())
            }
        }
    }

    override suspend fun fetchLinkPreview(
        url: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<LinkPreview?> {
        return performNetworkCall {
            runCatching {
                val response: JsonObject = ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments(*Api.Communication.path, "link-preview")
                    }
                    parameter("url", url)
                    cookieAuth(authToken)
                }.body()

                // For some reason, the server sometimes returns an empty response.
                if (response.jsonObject.isEmpty()) null
                else json.decodeFromJsonElement(LinkPreview.serializer(), response)
            }.getOrNull()
        }
    }

    override suspend fun getForwardedMessagesByIds(
        metisContext: MetisContext,
        postIds: List<Long>,
        postType: PostingType,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<ForwardedMessage>> {
        return performNetworkCall {
           val response = ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(*Api.Communication.path, "forwarded-messages")
                }
                parameter("postingIds", postIds.joinToString(","))
                parameter("type", postType.toString())
                cookieAuth(authToken)
            }

            // We are only interested in the actual forwarded messages.
            val messageWrapper: List<ForwardedMessagesResponse> = try {
                Json.decodeFromJsonElement(ListSerializer(ForwardedMessagesResponse.serializer()), response.body())
            } catch (e: SerializationException) {
                emptyList()
            }
            messageWrapper.flatMap { it.messages }
        }
    }

    override suspend fun createForwardedMessage(
        metisContext: MetisContext,
        forwardedMessage: ForwardedMessage,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<HttpResponse> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(*Api.Communication.path, "forwarded-messages")
                }

                contentType(ContentType.Application.Json)
                setBody(forwardedMessage)
                cookieAuth(authToken)
            }
        }
    }

    override suspend fun getPostsByIds(
        metisContext: MetisContext,
        postIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<StandalonePost>> {
        return performNetworkCall {
            runCatching {
                ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments(*Api.Communication.Courses.path)
                        appendPathSegments(metisContext.courseId.toString())
                        appendPathSegments("messages-source-posts")
                    }

                    parameter("postIds", postIds.joinToString(","))
                    cookieAuth(authToken)
                }.body<List<StandalonePost>>()
            }.getOrElse { emptyList() }
        }
    }

    override suspend fun getAnswerPostsByIds(
        metisContext: MetisContext,
        answerPostIds: List<Long>,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<List<AnswerPost>> {
        return performNetworkCall {
            runCatching {
                ktorProvider.ktorClient.get(serverUrl) {
                    url {
                        appendPathSegments(*Api.Communication.Courses.path)
                        appendPathSegments(metisContext.courseId.toString())
                        appendPathSegments("answer-messages-source-posts")
                    }

                    parameter("answerPostIds", answerPostIds.joinToString(","))
                    cookieAuth(authToken)
                }.body<List<AnswerPost>>()
            }.getOrElse { emptyList() }
        }
    }

    @Serializable
    private data class ForwardedMessagesResponse(
        val id: Long,
        val messages: List<ForwardedMessage>
    )
}
