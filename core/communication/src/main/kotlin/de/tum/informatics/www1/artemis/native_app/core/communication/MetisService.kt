package de.tum.informatics.www1.artemis.native_app.core.communication

import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import kotlinx.coroutines.flow.Flow

interface MetisService {

    /**
     * Performs a paged call to get the list of standalone posts available.
     */
    suspend fun getPosts(
        standalonePostsContext: StandalonePostsContext,
        pageNum: Int,
        authToken: String,
        serverUrl: String
    ): NetworkResponse<List<StandalonePost>>

    fun subscribeToPostUpdates(
        standalonePostsContext: StandalonePostsContext
    ): Flow<MetisPostDTO>

    /**
     * The metis context needed to query standalone posts.
     * @param query if not null the posts will be filtered to contain the given query.
     */
    data class StandalonePostsContext(
        val metisContext: MetisContext,
        val filter: List<MetisFilter>,
        val query: String?,
        val sortingStrategy: MetisSortingStrategy,
        val courseWideContext: CourseWideContext
    )
}