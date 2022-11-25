package de.tum.informatics.www1.artemis.native_app.core.communication.impl

import de.tum.informatics.www1.artemis.native_app.core.communication.*
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
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

                parameter("postSortCriterion", "CREATION_DATE")
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

                parameter(
                    "courseWideContext",
                    standalonePostsContext.courseWideContext.httpValue
                )

                if (standalonePostsContext.query != null) {
                    parameter("searchText", standalonePostsContext.query)
                }

                parameter(
                    "filterToUnresolved",
                    MetisFilter.RESOLVED !in standalonePostsContext.filter
                )
                parameter(
                    "filterToOwn",
                    MetisFilter.CREATED_BY_CLIENT in standalonePostsContext.filter
                )
                parameter(
                    "filterToAnsweredOrReacted",
                    MetisFilter.WITH_REACTION in standalonePostsContext.filter
                )

                parameter("pagingEnabled", true)
                parameter("page", pageNum)
                parameter("pageSize", 20)
            }.body()
        }
    }

    override fun subscribeToPostUpdates(standalonePostsContext: MetisService.StandalonePostsContext): Flow<MetisPostDTO> {
        val baseChannel = "/topic/metis"
        val channel = when (val mC = standalonePostsContext.metisContext) {
            is MetisContext.Conversation -> "/user$baseChannel/courses/${mC.courseId}/conversations/${mC.conversationId}"
            is MetisContext.Course -> "$baseChannel/courses/${mC.courseId}"
            is MetisContext.Exercise -> "$baseChannel/exercises/${mC.exerciseId}"
            is MetisContext.Lecture -> "$baseChannel/lectures/${mC.lectureId}"
        }

        return websocketProvider.subscribe(channel, MetisPostDTO.serializer())
    }
}