package de.tum.informatics.www1.artemis.native_app.core.datastore.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MetisDao {

    /**
     * Clears all data associated with the given metis context.
     */
    @Query(
        """
        delete from metis_post_context where server_id = :serverId and course_id = :courseId and lecture_id = :lectureId and exercise_id = :exerciseId
    """
    )
    suspend fun clearAll(serverId: String, courseId: Int, lectureId: Int, exerciseId: Int)

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
    suspend fun insertPostMetisContext(postMetisContext: MetisPostContextEntity)

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

    data class ReactionOnly(val authorId: Int, val emojiId: String)

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

    @Query(
        """
        delete from metis_post_context where 
        server_id = :host and
        server_post_id in (:serverPostIds)
    """
    )
    suspend fun deletePostingsWithServerIds(
        host: String,
        serverPostIds: List<Int>
    )

    @Query("""
        select
            mpc.client_post_id,
            mpc.server_post_id,
            p.content,
            p.creation_date,
            sp.context,
            sp.title,
            sp.resolved,
            u.name as author_name,
            p.author_role
        from 
            metis_post_context mpc, postings p, standalone_postings sp, users u
        where
            sp.post_id = :clientPostId and
            p.id = :clientPostId and
            sp.post_id = p.id and
            mpc.client_post_id = :clientPostId and
            u.server_id = mpc.server_id and
            u.id = p.author_id
    """)
    fun queryStandalonePost(clientPostId: String): Flow<Post?>

    @Query("select * from metis_post_context where client_post_id = :clientPostId")
    suspend fun queryMetisContextForStandalonePost(clientPostId: String): MetisPostContextEntity

    fun queryCoursePosts(
        serverId: String,
        courseId: Int,
        exerciseId: Int,
        lectureId: Int,
        metisFilter: List<MetisFilter>,
        metisSortingStrategy: MetisSortingStrategy,
        query: String?
    ): PagingSource<Int, Post> {
        val baseQuery = """
            select
                mpc.client_post_id,
                mpc.server_post_id,
                p.content,
                p.creation_date,
                sp.context,
                sp.title,
                sp.resolved,
                u.name as author_name,
                p.author_role
            from
                metis_post_context mpc,
                postings p,
                standalone_postings sp,
                users u
            where
                mpc.server_id = ? and
                mpc.course_id = ? and
                mpc.exercise_id = ? and
                mpc.lecture_id = ? and
                p.id = mpc.client_post_id and
                p.type = 'STANDALONE' and
                sp.post_id = p.id and
                u.server_id = mpc.server_id and
                u.id = p.author_id
            order by p.creation_date desc
        """.trimIndent()

        val constructedQuery = SimpleSQLiteQuery(
            baseQuery,
            arrayOf(
                serverId,
                courseId,
                exerciseId,
                lectureId
            )
        )

        return queryCoursePosts(constructedQuery)
    }

    @RawQuery(
        observedEntities = [
            BasePostingEntity::class,
            StandalonePostingEntity::class,
            AnswerPostingEntity::class,
            PostReactionEntity::class,
            StandalonePostTagEntity::class,
            MetisUserEntity::class
        ]
    )
    fun queryCoursePosts(query: SupportSQLiteQuery): PagingSource<Int, Post>

    @Transaction
    @Query("""
        select 
            count(*) 
        from 
            metis_post_context mpc, postings p, standalone_postings sp
        where 
            mpc.server_id = :serverId and
            mpc.course_id = :courseId and
            mpc.exercise_id = :exerciseId and
            mpc.lecture_id = :lectureId and
            p.id = mpc.client_post_id and
            p.type = 'STANDALONE' and
            sp.post_id = p.id and
            sp.is_live_created = 0
    """)
    suspend fun queryContextPostCountNoLiveCreated(serverId: String, courseId: Int, exerciseId: Int, lectureId: Int): Int

//    @Query(
//        """
//                select
//                    mpc.client_post_id,
//                    mpc.server_post_id,
//                    p.content,
//                    sp.title,
//                    sp.resolved,
//                    u.name as author_name,
//                    p.author_role
//                from
//                    metis_post_context mpc,
//                    postings p,
//                    standalone_postings sp,
//                    users u
//                where
//                    mpc.server_id = :serverId and
//                    mpc.course_id = :courseId and
//                    mpc.exercise_id = :exerciseId and
//                    mpc.lecture_id = :lectureId and
//                    p.id = mpc.client_post_id and
//                    p.type = 'standalone' and
//                    sp.post_id = p.id and
//                    u.server_id = mpc.server_id and
//                    u.id = p.author_id
//                order by p.creation_date desc
//    """
//    )
//    fun foo(serverId: String, courseId: Int, exerciseId: Int, lectureId: Int)
}