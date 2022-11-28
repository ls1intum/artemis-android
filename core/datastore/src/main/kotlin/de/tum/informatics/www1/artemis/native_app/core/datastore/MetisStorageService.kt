package de.tum.informatics.www1.artemis.native_app.core.datastore

import androidx.paging.PagingSource
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost

/**
 * Permanently store metis communication entities. Also allows reading from the metis storage service.
 * This class does not perform any networking.
 */
interface MetisStorageService {

    /**
     * Permanently store the given posts. If a post with an identical id already exists, the existing post is updated.
     * The answer posts are also updated by either being inserted or updated.
     */
    suspend fun insertOrUpdatePosts(host: String, metisContext: MetisContext, posts: List<StandalonePost>)

    /**
     * Deletes the posts with the given post ids. Can delete both answer and standalone posts.
     */
    suspend fun deletePosts(host: String, metisContext: MetisContext, postIds: List<Int>)

    /**
     * Deletes all posts and data associated with the given metis context.
     */
    suspend fun clearMetisContext(host: String, metisContext: MetisContext)

    fun getStoredPosts(
        serverId: String,
        filter: List<MetisFilter>,
        sortingStrategy: MetisSortingStrategy,
        query: String?,
        metisContext: MetisContext
    ): PagingSource<Int, Post>
}