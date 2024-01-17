package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage

import androidx.paging.PagingSource
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
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
     * Inserts the posts into the db that do not exist yet, and updates the ones that already exist, matching by server id.
     *
     * However, all posts between the oldest and newest post in [posts] that are not present in the list but exist in the db are removed.
     * If [removeAllOlderPosts] is set to true, all posts that are older than the oldest post in [posts] are also removed.
     */
    suspend fun insertOrUpdatePostsAndRemoveDeletedPosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>,
        removeAllOlderPosts: Boolean
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
        metisContext: MetisContext
    ): PagingSource<Int, PostPojo>

    fun getLatestKnownPost(serverId: String, metisContext: MetisContext): Flow<PostPojo?>

    /**
     * Query the given post with the given client post id.
     * Automatically emits when the post may have changed.
     */
    fun getStandalonePost(clientPostId: String): Flow<PostPojo?>

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