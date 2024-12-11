package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostTagEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface MetisDao {

    /**
     * Clears all data associated with the given metis context.
     */
    @Query(
        """
            delete from postings where server_id = :serverId
        """
    )
    suspend fun clearAll(serverId: String)

    /**
     * Query the client side post id for the given server side post id.
     * Returns null if the given post is not yet stored.
     * Note: Standalone posts and answer posts may have the same ids.
     */
    @Query(
        """
        select 
            client_post_id 
        from metis_post_context 
        where 
            server_id = :serverId and
            server_post_id = :postId and
            type = :postingType
    """
    )
    suspend fun queryClientPostId(
        serverId: String,
        postId: Long,
        postingType: BasePostingEntity.PostingType
    ): String?

    @Insert
    suspend fun insertPostMetisContext(postMetisContext: MetisPostContextEntity)

    @Query("select exists(select * from metis_post_context where client_post_id = :clientPostId and server_post_id = :serverPostId and course_id = :courseId and conversation_id = :conversationId)")
    suspend fun isPostPresentInContext(
        clientPostId: String,
        serverPostId: Long?,
        courseId: Long,
        conversationId: Long
    ): Boolean

    @Query("select exists(select * from metis_post_context where server_post_id = :serverPostId and course_id = :courseId and conversation_id = :conversationId and server_id = :serverId)")
    suspend fun isPostPresentInContext(
        serverId: String,
        serverPostId: Long,
        courseId: Long,
        conversationId: Long
    ): Boolean

    @Update
    suspend fun updateBasePost(post: BasePostingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBasePost(post: BasePostingEntity)

    @Update
    suspend fun updatePost(post: StandalonePostingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPost(post: StandalonePostingEntity)

    @Update
    suspend fun updateAnswerPosting(answerPost: AnswerPostingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnswerPosting(answerPost: AnswerPostingEntity)

    @Query("delete from reactions where post_id = :postId")
    suspend fun removeReactions(postId: String)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReactions(reactions: List<PostReactionEntity>)

    @Update
    suspend fun updateTags(tags: List<StandalonePostTagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<StandalonePostTagEntity>)

    @Query(
        """
        delete from post_tags where post_id = :postId and tag not in (:remainingTags)
    """
    )
    suspend fun removeSuperfluousTags(postId: String, remainingTags: List<String>)

    @Update
    suspend fun updateUser(user: MetisUserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: MetisUserEntity)

    @Update
    suspend fun updateUsers(users: List<MetisUserEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<MetisUserEntity>)

    @Query("update metis_post_context set server_post_id = :serverSidePostId where client_post_id = :clientSidePostId")
    suspend fun upgradePost(clientSidePostId: String, serverSidePostId: Long)

    @Query(
        """
        delete from metis_post_context where 
        server_id = :host and
        server_post_id in (:serverPostIds) and
        type = :postingType
    """
    )
    suspend fun deletePostingsWithServerIds(
        host: String,
        serverPostIds: List<Long>,
        postingType: BasePostingEntity.PostingType = BasePostingEntity.PostingType.STANDALONE
    )

    @Query("delete from postings where id = :clientPostId")
    suspend fun deletePostingWithClientSideId(
        clientPostId: String
    )

    @Query("""
        delete from metis_post_context where metis_post_context.server_id = :host and metis_post_context.course_id = :courseId and metis_post_context.conversation_id = :conversationId 
            and metis_post_context.server_post_id not in (:serverIds) and metis_post_context.server_post_id is not null and metis_post_context.type = :postingType
            and exists (
                select * from postings p where p.creation_date > :startInstant and p.creation_date < :endInstant and 
                p.id = metis_post_context.client_post_id
            )
    """)
    suspend fun deleteSuperfluousPostsInTimeInterval(
        host: String,
        courseId: Long,
        conversationId: Long,
        serverIds: List<Long>,
        startInstant: Instant,
        endInstant: Instant,
        postingType: BasePostingEntity.PostingType = BasePostingEntity.PostingType.STANDALONE
    )

    @Query("""
        delete from metis_post_context where metis_post_context.server_id = :host and metis_post_context.course_id = :courseId and metis_post_context.conversation_id = :conversationId 
            and metis_post_context.type = :postingType
            and exists (
                select * from postings p where p.creation_date < :threshold
                and p.id = metis_post_context.client_post_id
            )
    """)
    suspend fun deletePostsOlderThanThreshold(
        host: String,
        courseId: Long,
        conversationId: Long,
        threshold: Instant,
        postingType: BasePostingEntity.PostingType = BasePostingEntity.PostingType.STANDALONE
    )

    @Query(
        """
        delete from answer_postings where
        parent_post_id = :standalonePostClientId and 
        exists (
            select * from metis_post_context mpc where
            mpc.server_id = :host and
            mpc.client_post_id = post_id and
            mpc.server_post_id not in (:answerServerIds) and
            mpc.type = :postingType
        )
    """
    )
    suspend fun removeSuperfluousAnswerPosts(
        host: String,
        standalonePostClientId: String,
        answerServerIds: List<Long>,
        postingType: BasePostingEntity.PostingType = BasePostingEntity.PostingType.ANSWER
    )

    @Transaction
    @Query(
        """
        select
            mpc.client_post_id,
            mpc.server_post_id,
            p.content,
            p.creation_date,
            p.updated_date,
            sp.context,
            sp.title,
            sp.resolved,
            sp.display_priority,
            u.name as author_name,
            p.author_role,
            p.author_id as author_id,
            u.image_url as author_image_url
        from 
            metis_post_context mpc, postings p, standalone_postings sp, users u
        where
            sp.post_id = :clientPostId and
            p.id = :clientPostId and
            sp.post_id = p.id and
            mpc.client_post_id = :clientPostId and
            u.server_id = mpc.server_id and
            u.id = p.author_id
    """
    )
    fun queryStandalonePost(clientPostId: String): Flow<PostPojo?>

    @Query("""
        select
                mpc.client_post_id,
                mpc.server_post_id,
                p.content,
                p.creation_date,
                p.updated_date,
                sp.context,
                sp.title,
                sp.resolved,
                sp.display_priority,
                u.name as author_name,
                p.author_role,
                p.author_id as author_id,
                u.image_url as author_image_url
            from
                metis_post_context mpc,
                postings p,
                standalone_postings sp,
                users u
            where
                mpc.server_id = :serverId and
                mpc.course_id = :courseId and
                mpc.conversation_id = :conversationId and
                p.id = mpc.client_post_id and
                p.type = 'STANDALONE' and
                sp.post_id = p.id and
                u.server_id = mpc.server_id and
                u.id = p.author_id
            order by p.creation_date desc
    """)
    fun queryCoursePosts(
        serverId: String,
        courseId: Long,
        conversationId: Long
    ): PagingSource<Int, PostPojo>

    @Query("""
        select
                mpc.client_post_id,
                mpc.server_post_id,
                p.content,
                p.creation_date,
                p.updated_date,
                sp.context,
                sp.title,
                sp.resolved,
                sp.display_priority,
                u.name as author_name,
                p.author_role,
                p.author_id as author_id,
                u.image_url as author_image_url
            from
                metis_post_context mpc,
                postings p,
                standalone_postings sp,
                users u
            where
                mpc.server_id = :serverId and
                mpc.course_id = :courseId and
                mpc.conversation_id = :conversationId and
                p.id = mpc.client_post_id and
                p.type = 'STANDALONE' and
                sp.post_id = p.id and
                u.server_id = mpc.server_id and
                u.id = p.author_id and
                (:allowClientSidePost or mpc.server_post_id is not null)
            order by p.creation_date desc
            limit 1
    """)
    fun queryLatestKnownPost(
        serverId: String,
        courseId: Long,
        conversationId: Long,
        allowClientSidePost: Boolean
    ): Flow<PostPojo?>

    @Transaction
    @Query(
        """
        select 
            count(*) 
        from 
            metis_post_context mpc, postings p, standalone_postings sp
        where 
            mpc.server_id = :serverId and
            mpc.course_id = :courseId and
            mpc.conversation_id = :conversationId and
            p.id = mpc.client_post_id and
            p.type = 'STANDALONE' and
            sp.post_id = p.id and
            sp.live_created = 0
    """
    )
    suspend fun queryContextPostCountNoLiveCreated(
        serverId: String,
        courseId: Long,
        conversationId: Long
    ): Int

    @Query("select author_role from postings where id = :clientPostId")
    suspend fun queryPostAuthorRole(clientPostId: String): UserRole

    @Query("select server_post_id from metis_post_context where server_id = :serverId and client_post_id = :clientPostId")
    suspend fun queryServerSidePostId(serverId: String, clientPostId: String): Long
}
