package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.RESOURCE_PATH_SEGMENTS
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.appendPathSegments
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

internal class MetisServiceImpl(
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
}
