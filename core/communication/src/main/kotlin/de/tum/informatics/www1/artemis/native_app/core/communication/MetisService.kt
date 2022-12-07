package de.tum.informatics.www1.artemis.native_app.core.communication

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.Reaction
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import de.tum.informatics.www1.artemis.native_app.core.websocket.impl.WebsocketProvider
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
        serverSidePostId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<StandalonePost>

    fun subscribeToPostUpdates(
        metisContext: MetisContext
    ): Flow<WebsocketProvider.WebsocketData<MetisPostDTO>>

    /**
     * Returns null if no error occurred.
     */
    suspend fun createReaction(
        context: MetisContext,
        post: AffectedPost,
        emojiId: String,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Reaction>

    /**
     * Returns null if no error occurred.
     */
    suspend fun deleteReaction(
        context: MetisContext,
        reactionId: Int,
        serverUrl: String,
        authToken: String
    ): NetworkResponse<Unit>

    sealed class AffectedPost {
        data class Standalone(val postId: Int) : AffectedPost()
        data class Answer(val postId: Int) : AffectedPost()
    }

    /**
     * The metis context needed to query standalone posts.
     * @param query if not null the posts will be filtered to contain the given query.
     */
    data class StandalonePostsContext(
        val metisContext: MetisContext,
        val filter: List<MetisFilter>,
        val query: String?,
        val sortingStrategy: MetisSortingStrategy,
        val courseWideContext: CourseWideContext?
    )
}