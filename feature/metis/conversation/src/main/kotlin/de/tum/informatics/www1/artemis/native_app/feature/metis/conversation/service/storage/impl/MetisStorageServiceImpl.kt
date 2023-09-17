package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import androidx.paging.PagingSource
import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseWideContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.Reaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.MetisDao
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.AnswerPostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.BasePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisPostContextEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.MetisUserEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.PostReactionEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostTagEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.entities.StandalonePostingEntity
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * This implementation only displays live created posts, but ignores them when counting the posts for the next page request.
 */
class MetisStorageServiceImpl(
    private val databaseProvider: MetisDatabaseProvider
) : MetisStorageService {

    companion object {
        private val UserRole.asNetwork: UserRole
            get() = when (this) {
                UserRole.INSTRUCTOR -> UserRole.INSTRUCTOR
                UserRole.TUTOR -> UserRole.TUTOR
                UserRole.USER -> UserRole.USER
            }

        private val UserRole.asDb: UserRole
            get() = when (this) {
                UserRole.INSTRUCTOR -> UserRole.INSTRUCTOR
                UserRole.TUTOR -> UserRole.TUTOR
                UserRole.USER -> UserRole.USER
            }

        private val CourseWideContext.asDb: BasePostingEntity.CourseWideContext
            get() = when (this) {
                CourseWideContext.TECH_SUPPORT -> BasePostingEntity.CourseWideContext.TECH_SUPPORT
                CourseWideContext.ORGANIZATION -> BasePostingEntity.CourseWideContext.ORGANIZATION
                CourseWideContext.RANDOM -> BasePostingEntity.CourseWideContext.RANDOM
                CourseWideContext.ANNOUNCEMENT -> BasePostingEntity.CourseWideContext.ANNOUNCEMENT
            }

        private val DisplayPriority.asDb: BasePostingEntity.DisplayPriority
            get() = when (this) {
                DisplayPriority.PINNED -> BasePostingEntity.DisplayPriority.PINNED
                DisplayPriority.ARCHIVED -> BasePostingEntity.DisplayPriority.ARCHIVED
                DisplayPriority.NONE -> BasePostingEntity.DisplayPriority.NONE
            }

        private fun StandalonePost.asDb(
            serverId: String,
            clientSidePostId: String,
            isLiveCreated: Boolean
        ): Pair<BasePostingEntity, StandalonePostingEntity>? {
            val basePosting = BasePostingEntity(
                serverId = serverId,
                postId = clientSidePostId,
                postingType = BasePostingEntity.PostingType.STANDALONE,
                authorId = author?.id ?: return null,
                creationDate = creationDate ?: Instant.fromEpochSeconds(0),
                content = content,
                authorRole = authorRole?.asDb ?: UserRole.USER,
                updatedDate = updatedDate
            )

            val standalone = StandalonePostingEntity(
                postId = clientSidePostId,
                title = title,
                context = courseWideContext?.asDb,
                displayPriority = displayPriority?.asDb,
                resolved = resolved ?: false,
                liveCreated = isLiveCreated
            )

            return basePosting to standalone
        }

        private fun AnswerPost.asDb(
            serverId: String,
            clientSidePostId: String,
            parentClientSidePostId: String
        ): Triple<BasePostingEntity, AnswerPostingEntity, MetisUserEntity>? {
            val authorId = author?.id

            val basePost = BasePostingEntity(
                postId = clientSidePostId,
                serverId = serverId,
                postingType = BasePostingEntity.PostingType.ANSWER,
                authorId = authorId ?: return null,
                creationDate = creationDate ?: Instant.fromEpochSeconds(0),
                content = content,
                authorRole = authorRole?.asDb ?: UserRole.USER,
                updatedDate = updatedDate
            )

            val answer = AnswerPostingEntity(
                postId = clientSidePostId,
                parentPostId = parentClientSidePostId,
                resolvesPost = resolvesPost
            )

            val user = MetisUserEntity(
                serverId = serverId,
                id = authorId,
                displayName = author?.name ?: "NULL"
            )

            return Triple(basePost, answer, user)
        }

        private fun Reaction.asDb(
            serverId: String,
            clientSidePostId: String
        ): Pair<PostReactionEntity, MetisUserEntity>? {
            val userId = user?.id ?: return null

            return PostReactionEntity(
                serverId = serverId,
                postId = clientSidePostId,
                emojiId = emojiId,
                authorId = userId,
                id = id ?: return null
            ) to MetisUserEntity(
                serverId = serverId,
                id = userId,
                displayName = user?.name ?: return null
            )
        }

        private fun MetisContext.toPostMetisContext(
            serverId: String,
            clientSidePostId: String,
            serverSidePostId: Long,
            postingType: BasePostingEntity.PostingType
        ): MetisPostContextEntity =
            MetisPostContextEntity(
                serverId = serverId,
                courseId = courseId,
                exerciseId = exerciseId,
                lectureId = lectureId,
                serverPostId = serverSidePostId,
                clientPostId = clientSidePostId,
                postingType = postingType
            )

        private val MetisContext.lectureId: Long get() = if (this is MetisContext.Lecture) lectureId else -1
        private val MetisContext.exerciseId: Long get() = if (this is MetisContext.Exercise) exerciseId else -1
    }

    override suspend fun insertOrUpdatePosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>,
        clearPreviousPosts: Boolean
    ) {
        val metisDao = databaseProvider.metisDao

        databaseProvider.database.withTransaction {
            if (clearPreviousPosts) {
                metisDao.clearAll(
                    host
                )
            }

            // Extract the db entities from the network entities. With the schema invalid entities are discarded.
            for (sp in posts) {
                val queryClientPostId = metisDao.queryClientPostId(
                    serverId = host,
                    postId = sp.id ?: continue,
                    postingType = BasePostingEntity.PostingType.STANDALONE
                )

                insertOrUpdatePost(
                    metisDao,
                    host,
                    metisContext,
                    queryClientPostId,
                    sp,
                    isLiveCreated = false
                )
            }
        }
    }

    override suspend fun updatePost(
        host: String,
        metisContext: MetisContext,
        post: StandalonePost
    ) {
        val metisDao = databaseProvider.metisDao
        databaseProvider.database.withTransaction {
            val clientSidePostId = metisDao.queryClientPostId(
                serverId = host,
                postId = post.id ?: return@withTransaction,
                postingType = BasePostingEntity.PostingType.STANDALONE
            ) ?: return@withTransaction

            //The author role seems to be omitted when sending an update. Therefore, we just load the one still cached.
            val actPost = if (post.authorRole == null) {
                val storedRole = metisDao.queryPostAuthorRole(clientSidePostId)
                post.copy(authorRole = storedRole)
            } else post

            insertOrUpdatePost(
                metisDao,
                host,
                metisContext,
                clientSidePostId,
                actPost,
                isLiveCreated = false
            )
        }
    }

    override suspend fun insertLiveCreatedPost(
        host: String,
        metisContext: MetisContext,
        post: StandalonePost
    ): String? {
        val metisDao = databaseProvider.metisDao
        return databaseProvider.database.withTransaction {
            // First check, if by any chance there is a client side post id for this already.
            // If the post already exists, do nothing
            metisDao.queryClientPostId(
                serverId = host,
                postId = post.id ?: 0L,
                postingType = BasePostingEntity.PostingType.STANDALONE
            ) ?: insertOrUpdatePost(metisDao, host, metisContext, null, post, true)
        }
    }

    /**
     * @return the client side post id or null if failed
     */
    private suspend fun insertOrUpdatePost(
        metisDao: MetisDao,
        host: String,
        metisContext: MetisContext,
        queryClientPostId: String?,
        sp: StandalonePost,
        isLiveCreated: Boolean
    ): String? {
        val postingAuthor = MetisUserEntity(
            serverId = host,
            id = sp.author?.id ?: return null,
            displayName = sp.author?.name ?: return null
        )

        val clientSidePostId: String = queryClientPostId ?: UUID.randomUUID().toString()

        val (standaloneBasePosting, standalonePosting) = sp.asDb(
            serverId = host,
            clientSidePostId = clientSidePostId,
            isLiveCreated = isLiveCreated
        ) ?: return null

        val standalonePostReactionsWithUsers =
            sp.reactions.orEmpty().mapNotNull { r -> r.asDb(host, clientSidePostId) }
        val standalonePostReactions = standalonePostReactionsWithUsers.map { it.first }
        val standalonePostReactionsUsers =
            standalonePostReactionsWithUsers.map { it.second }

        val tags = sp.tags.orEmpty().map { tag ->
            StandalonePostTagEntity(
                postId = clientSidePostId,
                tag = tag
            )
        }

        //First insert the users as they have no dependencies
        metisDao.updateUsers(standalonePostReactionsUsers)
        metisDao.insertUsers(standalonePostReactionsUsers)

        metisDao.insertOrUpdateUser(postingAuthor)

        val standalonePostId = sp.id
        val postMetisContext =
            metisContext.toPostMetisContext(
                serverId = host,
                clientSidePostId = clientSidePostId,
                serverSidePostId = standalonePostId ?: return null,
                postingType = BasePostingEntity.PostingType.STANDALONE
            )

        if (queryClientPostId != null) {
            val isPostPresent = metisDao.isPostPresentInContext(
                queryClientPostId,
                standalonePostId,
                courseId = postMetisContext.courseId,
                exerciseId = postMetisContext.exerciseId,
                lectureId = postMetisContext.lectureId
            )

            if (!isPostPresent) {
                metisDao.insertPostMetisContext(
                    postMetisContext
                )
            }

            metisDao.updateBasePost(standaloneBasePosting)
            metisDao.updatePost(standalonePosting)

            metisDao.updateReactions(clientSidePostId, standalonePostReactions)

            metisDao.insertTags(tags)
            metisDao.removeSuperfluousTags(clientSidePostId, tags.map { it.tag })
            metisDao.removeSuperfluousAnswerPosts(
                host,
                clientSidePostId,
                sp.answers.orEmpty().mapNotNull { it.id }
            )
        } else {
            metisDao.insertBasePost(standaloneBasePosting)
            metisDao.insertPost(standalonePosting)
            metisDao.insertReactions(standalonePostReactions)
            metisDao.insertTags(tags)

            metisDao.insertPostMetisContext(
                postMetisContext
            )
        }

        for (ap in sp.answers.orEmpty()) {
            val answerPostId = ap.id

            val queryClientPostIdAnswer = metisDao.queryClientPostId(
                serverId = host,
                postId = answerPostId ?: return null,
                postingType = BasePostingEntity.PostingType.ANSWER
            )
            val answerClientSidePostId =
                queryClientPostIdAnswer ?: UUID.randomUUID().toString()

            val authorRole = (if (queryClientPostIdAnswer != null && ap.authorRole == null) {
                metisDao.queryPostAuthorRole(answerClientSidePostId)
            } else ap.authorRole?.asDb)?.asNetwork

            val (basePostingEntity, answerPostingEntity, metisUserEntity) = ap
                .copy(authorRole = authorRole)
                .asDb(
                    serverId = host,
                    clientSidePostId = answerClientSidePostId,
                    parentClientSidePostId = clientSidePostId
                ) ?: return null

            val answerPostReactionsWithUsers =
                ap.reactions.orEmpty().mapNotNull { it.asDb(host, answerClientSidePostId) }
            val answerPostReactions = answerPostReactionsWithUsers.map { it.first }
            val answerPostReactionUsers = answerPostReactionsWithUsers.map { it.second }

            metisDao.insertOrUpdateUser(metisUserEntity)
            metisDao.updateUsers(answerPostReactionUsers)
            metisDao.insertUsers(answerPostReactionUsers)

            if (queryClientPostIdAnswer != null) {
                metisDao.updateBasePost(basePostingEntity)
                metisDao.updateAnswerPosting(answerPostingEntity)

                metisDao.updateReactions(answerClientSidePostId, answerPostReactions)
            } else {
                metisDao.insertBasePost(basePostingEntity)
                metisDao.insertAnswerPosting(answerPostingEntity)
                metisDao.insertReactions(answerPostReactions)
                metisDao.insertPostMetisContext(
                    metisContext.toPostMetisContext(
                        serverId = host,
                        clientSidePostId = answerClientSidePostId,
                        serverSidePostId = answerPostId,
                        postingType = BasePostingEntity.PostingType.ANSWER
                    )
                )
            }
        }

        return clientSidePostId
    }

    private suspend fun MetisDao.insertOrUpdateUser(user: MetisUserEntity) {
        updateUser(user)
        insertUser(user)
    }

    /**
     * Insert new reactions and remove reactions no longer present
     */
    private suspend fun MetisDao.updateReactions(
        postId: String,
        reactions: List<PostReactionEntity>
    ) {
        removeReactions(postId)
        insertReactions(reactions)
    }

    override suspend fun deletePosts(host: String, postIds: List<Long>) {
        val metisDao = databaseProvider.metisDao
        databaseProvider.database.withTransaction {
            metisDao.deletePostingsWithServerIds(host, postIds)
        }
    }

    override fun getStoredPosts(
        serverId: String,
        clientId: Long,
        filter: List<MetisFilter>,
        sortingStrategy: MetisSortingStrategy,
        query: String?,
        metisContext: MetisContext
    ): PagingSource<Int, Post> {
        return databaseProvider.metisDao.queryCoursePosts(
            serverId = serverId,
            clientId = clientId,
            courseId = metisContext.courseId,
            exerciseId = metisContext.exerciseId,
            lectureId = metisContext.lectureId,
            metisFilter = filter,
            metisSortingStrategy = sortingStrategy,
            query = query
        )
    }

    override fun getStandalonePost(clientPostId: String): Flow<Post?> {
        return databaseProvider.metisDao.queryStandalonePost(clientPostId)
    }

    override suspend fun getCachedPostCount(host: String, metisContext: MetisContext): Int {
        return databaseProvider.metisDao.queryContextPostCountNoLiveCreated(
            host,
            metisContext.courseId,
            metisContext.exerciseId,
            metisContext.lectureId
        )
    }

    override suspend fun getServerSidePostId(host: String, clientSidePostId: String): Long {
        return databaseProvider.metisDao.queryServerSidePostId(host, clientSidePostId)
    }

    override suspend fun getClientSidePostId(
        host: String,
        serverSidePostId: Long,
        postingType: BasePostingEntity.PostingType
    ): String? {
        return databaseProvider.metisDao
            .queryClientPostId(
                serverId = host,
                postId = serverSidePostId,
                postingType = postingType
            )
    }
}