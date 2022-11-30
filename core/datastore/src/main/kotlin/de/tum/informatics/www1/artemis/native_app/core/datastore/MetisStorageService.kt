package de.tum.informatics.www1.artemis.native_app.core.datastore

import androidx.paging.PagingSource
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.model.metis.StandalonePost
import kotlinx.coroutines.flow.Flow

/**
 * Permanently store metis communication entities. Also allows reading from the metis storage service.
 * This class does not perform any networking.
 */
interface MetisStorageService {

    /**
     * Permanently store the given posts. If a post with an identical id already exists, the existing post is updated.
     * The answer posts are also updated by either being inserted or updated.
     *
     * @param clearPreviousPosts if all posts matching the context should be cleared before anything being inserted.
     */
    suspend fun insertOrUpdatePosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>,
        clearPreviousPosts: Boolean
    )

    /**
     * Update the post with the same id in the database. If this post does not exist, no action is taken.
     */
    suspend fun updatePost(host: String, metisContext: MetisContext, post: StandalonePost)

    /**
     * Insert a post that has been created live and are thus not sent by a regular rest request.
     */
    suspend fun insertLiveCreatedPost(host: String, metisContext: MetisContext, post: StandalonePost)

    /**
     * Deletes the posts with the given post ids. Can delete both answer and standalone posts.
     */
    suspend fun deletePosts(host: String, postIds: List<Int>)

    fun getStoredPosts(
        serverId: String,
        filter: List<MetisFilter>,
        sortingStrategy: MetisSortingStrategy,
        query: String?,
        metisContext: MetisContext
    ): PagingSource<Int, Post>

    /**
     * Query the given post with the given client post id.
     * Automatically emits when the post may have changed.
     */
    fun getStandalonePost(clientPostId: String): Flow<Post?>

    /**
     * Query the corresponding metis context to the post
     */
    suspend fun getStandalonePostMetisContext(clientPostId: String): MetisContext

    /**
     * Return the amount of posts cached from the rest calls.
     * WARNING: This does ignore live created posts.
     */
    suspend fun getCachedPostCount(host: String, metisContext: MetisContext): Int
}