package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import de.tum.informatics.www1.artemis.native_app.core.communication.*
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow

class MetisServiceImpl(
    private val networkStatusProvider: NetworkStatusProvider,
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
                    appendPathSegments("api", "courses")
                    appendPathSegments(metisContext.courseId.toString())
                    appendPathSegments(metisContext.resourceEndpoint)
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