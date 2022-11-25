package de.tum.informatics.www1.artemis.native_app.core.datastore.dao

import androidx.paging.DataSource
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.paging.LimitOffsetPagingSource
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*

@Dao
interface MetisDao {

    @Update
    suspend fun updateBasePosts(posts: List<PostingEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBasePosts(posts: List<PostingEntity>)

    @Update
    suspend fun updatePosts(posts: List<StandalonePosting>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPosts(posts: List<StandalonePosting>)

    @Update
    suspend fun updateAnswerPostings(answerPosts: List<AnswerPosting>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnswerPostings(answerPosts: List<AnswerPosting>)

    @Update
    suspend fun updateReactions(reactions: List<PostReaction>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReactions(reactions: List<PostReaction>)

    @Update
    suspend fun updateTags(tags: List<StandalonePostTag>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<StandalonePostTag>)

    @Update
    suspend fun updateUsers(users: List<MetisUserEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<MetisUserEntity>)

    /**
     * Inserts all entities unknown yet and updates the one known already.
     * All database updates are executed within a transaction.
     */
    @Transaction
    suspend fun insertOrUpdatePosts(
        basePostings: List<PostingEntity>,
        standalonePosts: List<StandalonePosting>,
        answerPosts: List<AnswerPosting>,
        reactions: List<PostReaction>,
        tags: List<StandalonePostTag>,
        users: List<MetisUserEntity>
    ) {
        //First perform the update, it ignores the rows that do not exist yet
        //Then perform the insert, which ignores the rows which already exist

        updateBasePosts(basePostings)
        insertBasePosts(basePostings)

        updatePosts(standalonePosts)
        insertPosts(standalonePosts)

        updateAnswerPostings(answerPosts)
        insertAnswerPostings(answerPosts)

        updateReactions(reactions)
        insertReactions(reactions)

        updateTags(tags)
        insertTags(tags)

        updateUsers(users)
        insertUsers(users)
    }

    @Query("select 'abc' from postings")
    fun fooQuery(): PagingSource<Int, String>

    fun queryPosts(
        serverId: String,
        courseId: Int,
        exerciseId: Int?,
        lectureId: Int?,
        query: String
    ): PagingSource<Int, Post> {
        return object : LimitOffsetPagingSource() {

        }
    }
}