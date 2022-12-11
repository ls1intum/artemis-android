package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import de.tum.informatics.www1.artemis.native_app.core.communication.*
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.model.metis.AnswerPost
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.Reaction
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow

class MetisServiceImpl(
    private val ktorProvider: KtorProvider,
    private val websocketProvider: WebsocketProvider
) : MetisService {

    companion object {
        private val RESOURCE_PATH_SEGMENTS = listOf("api", "courses")
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
                    appendPathSegments(metisContext.standalonePostResourceEndpoint)
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
                    is MetisContext.Exercise -> {
                        parameter("exerciseId", metisContext.exerciseId)
                    }

                    is MetisContext.Lecture -> {
                        parameter("lectureId", metisContext.lectureId)
                    }

                    is MetisContext.Conversation -> {
                        parameter("conversationId", metisContext.conversationId)
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
                        MetisFilter.UNRESOLVED in standalonePostsContext.filter
                    )
                    parameter(
                        "filterToOwn",
                        MetisFilter.CREATED_BY_CLIENT in standalonePostsContext.filter
                    )
                    parameter(
                        "filterToAnsweredOrReacted",
                        MetisFilter.WITH_REACTION in standalonePostsContext.filter
                    )
                }

                parameter("pagingEnabled", true)
                parameter("page", pageNum)
                parameter("pageSize", pageSize)

                bearerAuth(authToken)
            }.body()
        }
    }

    /**
     * Uses the fact that you can query a single post using the query parameter.
     * Therefore, no extra API is required.
     */
    override suspend fun getPost(
        metisContext: MetisContext,
        serverSidePostId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        val posts = getPosts(
            standalonePostsContext = MetisService.StandalonePostsContext(
                metisContext = metisContext,
                filter = emptyList(),
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

    override fun subscribeToPostUpdates(metisContext: MetisContext): Flow<WebsocketProvider.WebsocketData<MetisPostDTO>> {
        val baseChannel = "/topic/metis"
        val channel = when (metisContext) {
            is MetisContext.Conversation -> "/user$baseChannel/courses/${metisContext.courseId}/conversations/${metisContext.conversationId}"
            is MetisContext.Course -> "$baseChannel/courses/${metisContext.courseId}"
            is MetisContext.Exercise -> "$baseChannel/exercises/${metisContext.exerciseId}"
            is MetisContext.Lecture -> "$baseChannel/lectures/${metisContext.lectureId}"
        }

        return websocketProvider.subscribe(channel, MetisPostDTO.serializer())
    }

    override suspend fun createPost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(context.courseId.toString(), context.standalonePostResourceEndpoint)
                }

                contentType(ContentType.Application.Json)

                setBody(post)
                bearerAuth(authToken)
            }.body()
        }
    }

    override suspend fun createAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost> {
        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(context.courseId.toString(), context.answerPostResourceEndpoint)
                }

                contentType(ContentType.Application.Json)

                setBody(post)
                bearerAuth(authToken)
            }.body()
        }
    }

    override suspend fun updatePost(
        context: MetisContext,
        post: StandalonePost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(context.courseId.toString(), context.standalonePostResourceEndpoint)
                    appendPathSegments(post.id.toString())
                }

                setBody(post)
                bearerAuth(authToken)
            }.body()
        }
    }

    override suspend fun updateAnswerPost(
        context: MetisContext,
        post: AnswerPost,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<AnswerPost> {
        return performNetworkCall {
            ktorProvider.ktorClient.put(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(context.courseId.toString(), context.answerPostResourceEndpoint)
                    appendPathSegments(post.id.toString())
                }

                setBody(post)
                bearerAuth(authToken)
            }.body()
        }
    }

    override suspend fun createReaction(
        context: MetisContext,
        post: MetisService.AffectedPost,
        emojiId: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Reaction> {
        val reaction = when (post) {
            is MetisService.AffectedPost.Answer -> {
                Reaction(
                    emojiId = emojiId,
                    answerPost = AnswerPost(id = post.postId)
                )
            }

            is MetisService.AffectedPost.Standalone -> {
                Reaction(
                    emojiId = emojiId,
                    standalonePost = StandalonePost(id = post.postId)
                )
            }
        }

        return performNetworkCall {
            ktorProvider.ktorClient.post(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        "postings",
                        "reactions"
                    )
                }

                setBody(reaction)
                bearerAuth(authToken)
                contentType(ContentType.Application.Json)
            }.body()
        }
    }

    override suspend fun deleteReaction(
        context: MetisContext,
        reactionId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit> {
        return performNetworkCall {
            ktorProvider.ktorClient.delete(serverUrl) {
                url {
                    appendPathSegments(RESOURCE_PATH_SEGMENTS)
                    appendPathSegments(
                        context.courseId.toString(),
                        "postings",
                        "reactions",
                        reactionId.toString()
                    )
                }

                bearerAuth(authToken)
                contentType(ContentType.Application.Json)
            }

            Unit
        }
    }
}