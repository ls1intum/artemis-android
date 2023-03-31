package de.tum.informatics.www1.artemis.native_app.feature.metis.dao

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import de.tum.informatics.www1.artemis.native_app.core.model.metis.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

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
     * Query the client side post if for the given metis context. Returns null if the given post is not yet stored.
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
        postingType: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity.PostingType
    ): String?

    @Insert
    suspend fun insertPostMetisContext(postMetisContext: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisPostContextEntity)

    @Query("select exists(select * from metis_post_context where client_post_id = :clientPostId and server_post_id = :serverPostId and course_id = :courseId and exercise_id = :exerciseId and lecture_id = :lectureId)")
    suspend fun isPostPresentInContext(
        clientPostId: String,
        serverPostId: Long,
        courseId: Long,
        exerciseId: Long,
        lectureId: Long
    ): Boolean

    @Update
    suspend fun updateBasePost(post: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBasePost(post: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity)

    @Update
    suspend fun updatePost(post: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPost(post: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostingEntity)

    @Update
    suspend fun updateAnswerPosting(answerPost: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.AnswerPostingEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnswerPosting(answerPost: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.AnswerPostingEntity)

    @Query("delete from reactions where post_id = :postId")
    suspend fun removeReactions(postId: String)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReactions(reactions: List<de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.PostReactionEntity>)

    @Update
    suspend fun updateTags(tags: List<de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostTagEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostTagEntity>)

    @Query(
        """
        delete from post_tags where post_id = :postId and tag not in (:remainingTags)
    """
    )
    suspend fun removeSuperfluousTags(postId: String, remainingTags: List<String>)

    @Update
    suspend fun updateUser(user: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisUserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisUserEntity)

    @Update
    suspend fun updateUsers(users: List<de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisUserEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisUserEntity>)

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
        postingType: de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity.PostingType = de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity.PostingType.STANDALONE
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
    """
    )
    fun queryStandalonePost(clientPostId: String): Flow<de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post?>

    fun queryCoursePosts(
        serverId: String,
        courseId: Long,
        exerciseId: Long,
        lectureId: Long,
        clientId: Long,
        metisFilter: List<de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter>,
        metisSortingStrategy: de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy,
        query: String?
    ): PagingSource<Int, de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post> {
        val queryReplyCount =
            "(select count(*) from answer_postings ap where ap.parent_post_id = sp.post_id)"
        val queryEmojiCount = "(select count(*) from reactions r where r.post_id = p.id)"

        val orderBy = when (metisSortingStrategy) {
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy.DATE_ASCENDING -> "order by p.creation_date asc"
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy.DATE_DESCENDING -> "order by p.creation_date desc"
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy.REPLIES_ASCENDING -> "order by $queryReplyCount asc"
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy.REPLIES_DESCENDING -> "order by $queryReplyCount desc"
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy.VOTES_ASCENDING -> "order by $queryEmojiCount asc"
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy.VOTES_DESCENDING -> "order by $queryEmojiCount desc"
        }

        val metisFilterSql = buildString {
            if (de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter.UNRESOLVED in metisFilter) {
                append("and sp.resolved = 0 \n")
            }
            if (de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter.CREATED_BY_CLIENT in metisFilter) {
                append("and p.author_id = ? \n")
            }
            if (de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter.WITH_REACTION in metisFilter) {
                append("and ($queryReplyCount > 0 or \n")
                append("(select count(*) from reactions r where r.post_id = p.id) > 0)  \n")
            }
        }

        val bindServerPostId =
            query != null && query.startsWith("#") && query.substring(1).isNotBlank()
        val queryServerPostId =
            if (query != null && bindServerPostId) query.substring(1).toIntOrNull() ?: "" else ""

        val likeQueryLiteral = "%${query?.lowercase(Locale.getDefault())}%"

        val querySql = buildString {
            if (query != null) {
                if (bindServerPostId) {
                    append("and mpc.server_post_id = ?  \n")
                } else {
                    append("and (sp.title like ? or p.content like ? or \n")
                    append("exists (select * from post_tags pt where pt.tag like ?))")
                }
            }
        }

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
                $metisFilterSql
                $querySql
            $orderBy
        """.trimIndent()

        val constructedQuery = SimpleSQLiteQuery(
            baseQuery,
            arrayOf(
                serverId,
                courseId,
                exerciseId,
                lectureId
            )
                    + (if (de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisFilter.CREATED_BY_CLIENT in metisFilter) arrayOf(clientId) else emptyArray())
                    + (if (bindServerPostId) arrayOf(queryServerPostId) else emptyArray())
                    + if (!bindServerPostId && query != null) arrayOf(
                likeQueryLiteral,
                likeQueryLiteral,
                likeQueryLiteral
            ) else emptyArray()
        )

        return queryCoursePosts(constructedQuery)
    }

    @RawQuery(
        observedEntities = [
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.BasePostingEntity::class,
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostingEntity::class,
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.AnswerPostingEntity::class,
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.PostReactionEntity::class,
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.StandalonePostTagEntity::class,
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisUserEntity::class,
            de.tum.informatics.www1.artemis.native_app.feature.metis.model.room.MetisPostContextEntity::class
        ]
    )
    fun queryCoursePosts(query: SupportSQLiteQuery): PagingSource<Int, de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post>

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
            mpc.exercise_id = :exerciseId and
            mpc.lecture_id = :lectureId and
            p.id = mpc.client_post_id and
            p.type = 'STANDALONE' and
            sp.post_id = p.id and
            sp.live_created = 0
    """
    )
    suspend fun queryContextPostCountNoLiveCreated(
        serverId: String,
        courseId: Long,
        exerciseId: Long,
        lectureId: Long
    ): Int

    @Query("select author_role from postings where id = :clientPostId")
    suspend fun queryPostAuthorRole(clientPostId: String): UserRole

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
//                    u.id = p.author_id and
//                    sp.resolved = 1 and
//                    exists (select count(*) from reactions r where r.post_id = p.id )
//                order by (select count(*) from reactions r where r.post_id = p.id) desc
//    """
//    )
//    fun foo(serverId: String, courseId: Long, exerciseId: Long, lectureId: Long)

    @Query("select server_post_id from metis_post_context where server_id = :serverId and client_post_id = :clientPostId")
    suspend fun queryServerSidePostId(serverId: String, clientPostId: String): Long
}