package de.tum.informatics.www1.artemis.native_app.feature.metis.service

import androidx.paging.PagingSource
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity
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
     * @return the client side post id of the created post or null, if the creation of the post failed
     */
    suspend fun insertLiveCreatedPost(host: String, metisContext: MetisContext, post: StandalonePost): String?

    /**
     * Deletes the posts with the given post ids. Can delete both answer and standalone posts.
     */
    suspend fun deletePosts(host: String, postIds: List<Long>)

    fun getStoredPosts(
        serverId: String,
        clientId: Long,
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
     * Return the amount of posts cached from the rest calls.
     * WARNING: This does ignore live created posts.
     */
    suspend fun getCachedPostCount(host: String, metisContext: MetisContext): Int

    suspend fun getServerSidePostId(host: String, clientSidePostId: String): Long

    suspend fun getClientSidePostId(
        host: String,
        serverSidePostId: Long,
        postingType: BasePostingEntity.PostingType
    ): String?
}