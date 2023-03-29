package de.tum.informatics.www1.artemis.native_app.feature.metis.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.Flow

internal class MetisServiceImpl(
    private val ktorProvider: KtorProvider,
    private val websocketProvider: WebsocketProvider
) : MetisService {

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
}
