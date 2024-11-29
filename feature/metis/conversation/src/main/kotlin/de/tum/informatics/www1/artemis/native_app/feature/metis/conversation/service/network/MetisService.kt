package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisPostDTO
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import kotlinx.coroutines.flow.Flow

interface MetisService {

    /**
     * Performs a paged call to get the list of standalone posts available.
     */
    suspend fun getPosts(
        standalonePostsContext: StandalonePostsContext,
        pageSize: Int,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<StandalonePost>>

    /**
     * Request a single post from the server.
     */
    suspend fun getPost(
        metisContext: MetisContext,
        serverSidePostId: Long,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost>

    fun subscribeToPostUpdates(
        courseId: Long,
        clientId: Long,
    ): Flow<WebsocketProvider.WebsocketData<MetisPostDTO>>

    /**
     * The metis context needed to query standalone posts.
     * @param query if not null the posts will be filtered to contain the given query.
     */
    data class StandalonePostsContext(
        val metisContext: MetisContext,
        val filter: List<MetisFilter>,
        val query: String?,
        val sortingStrategy: MetisSortingStrategy = MetisSortingStrategy.DATE_DESCENDING,
        val courseWideContext: CourseWideContext? = null
    )
}