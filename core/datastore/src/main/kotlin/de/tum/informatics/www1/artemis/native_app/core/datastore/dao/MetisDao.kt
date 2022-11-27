package de.tum.informatics.www1.artemis.native_app.core.datastore.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*

@Dao
interface MetisDao {

    /**
     * Query the client side post if for the given metis context. Returns null if the given post is not yet stored.
     */
    @Query(
        """
        select 
            client_post_id 
        from metis_post_context 
        where 
            server_id = :serverId and 
            course_id = :courseId and
            exercise_id = :exerciseId and
            lecture_id = :lectureId and
            server_post_id = :postId
    """
    )
    suspend fun queryClientPostId(
        serverId: String,
        courseId: Int,
        exerciseId: Int,
        lectureId: Int,
        postId: Int
    ): String?

    @Insert
    suspend fun insertPostMetisContext(postMetisContext: PostMetisContext)

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

    @Query("""
        delete from reactions where
            post_id = :postId and
            (select author_id, emoji) not in (:remainingReactions)
    """)
    suspend fun removeSuperfluousReactions(postId: String, remainingReactions: List<ReactionOnly>)

    data class ReactionOnly(val authorId: Int, val emojiId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReactions(reactions: List<PostReactionEntity>)

    @Update
    suspend fun updateTags(tags: List<StandalonePostTagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<StandalonePostTagEntity>)

    @Query("""
        delete from post_tags where post_id = :postId and tag not in (:remainingTags)
    """)
    suspend fun removeSuperfluousTags(postId: String, remainingTags: List<String>)

    @Update
    suspend fun updateUser(user: MetisUserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: MetisUserEntity)

    @Update
    suspend fun updateUsers(users: List<MetisUserEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<MetisUserEntity>)

    @Query(
        """
            
        """
    )
    fun queryPosts(
        serverId: String,
        courseId: Int,
        exerciseId: Int?,
        lectureId: Int?,
        query: String
    ): PagingSource<Int, Post>
}