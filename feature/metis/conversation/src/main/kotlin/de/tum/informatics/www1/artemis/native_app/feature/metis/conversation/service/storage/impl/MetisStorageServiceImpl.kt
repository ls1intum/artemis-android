package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.MetisDatabaseProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.BasePost
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
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * This implementation only displays live created posts, but ignores them when counting the posts for the next page request.
 */
internal class MetisStorageServiceImpl(
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
                creationDate = creationDate,
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
            serverSidePostId: Long?,
            postingType: BasePostingEntity.PostingType
        ): MetisPostContextEntity =
            MetisPostContextEntity(
                serverId = serverId,
                courseId = courseId,
                conversationId = conversationId,
                serverPostId = serverSidePostId,
                clientPostId = clientSidePostId,
                postingType = postingType
            )

        private val MetisContext.conversationId: Long get() = if (this is MetisContext.Conversation) conversationId else -1
    }

    override suspend fun insertOrUpdatePosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>
    ) {
        val metisDao = databaseProvider.metisDao

        databaseProvider.database.withTransaction {
            insertOrUpdateImpl(posts, metisDao, host, metisContext)
        }
    }

    /**
     * Inserts or updates the posts in the given list.
     */
    private suspend fun insertOrUpdateImpl(
        posts: List<StandalonePost>,
        metisDao: MetisDao,
        host: String,
        metisContext: MetisContext
    ) {
        // Extract the db entities from the network entities. With the schema invalid entities are discarded.
        for (sp in posts) {
            val queryClientPostId = metisDao.queryClientPostId(
                serverId = host,
                postId = sp.id ?: continue,
                postingType = BasePostingEntity.PostingType.STANDALONE
            )

            insertOrUpdatePost(
                metisDao = metisDao,
                isNewPost = queryClientPostId == null,
                host = host,
                metisContext = metisContext,
                clientSidePostId = queryClientPostId ?: UUID.randomUUID().toString(),
                sp = sp,
                isLiveCreated = false
            )
        }
    }

    override suspend fun insertOrUpdatePostsAndRemoveDeletedPosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>,
        removeAllOlderPosts: Boolean
    ) {
        val metisDao = databaseProvider.metisDao

        // We do not have to do anything
        if (posts.isEmpty()) return

        val sortedByDate = posts
            .sortedByDescending { it.creationDate }

        val oldestPost = sortedByDate.last()
        val newestPost = sortedByDate.first()

        databaseProvider.database.withTransaction {
            insertOrUpdateImpl(posts, metisDao, host, metisContext)

            // Remove posts that do no longer exist.
            metisDao.deleteSuperfluousPostsInTimeInterval(
                host = host,
                courseId = metisContext.courseId,
                conversationId = metisContext.conversationId,
                serverIds = posts.mapNotNull { it.serverPostId },
                startInstant = oldestPost.creationDate,
                endInstant = newestPost.creationDate
            )

            if (removeAllOlderPosts) {
                metisDao.deletePostsOlderThanThreshold(
                    host = host,
                    courseId = metisContext.courseId,
                    conversationId = metisContext.conversationId,
                    threshold = oldestPost.creationDate
                )
            }
        }
    }

    override suspend fun insertClientSidePost(
        host: String,
        metisContext: MetisContext,
        post: BasePost,
        clientSidePostId: String
    ) {
        val metisDao = databaseProvider.metisDao

        databaseProvider.database.withTransaction {
            when (post) {
                is StandalonePost -> {
                    insertOrUpdatePost(
                        metisDao = databaseProvider.metisDao,
                        isNewPost = true,
                        host = host,
                        metisContext = metisContext,
                        clientSidePostId = clientSidePostId,
                        sp = post,
                        isLiveCreated = false
                    )
                }

                is AnswerPost -> {
                    // First figure out the parent and then insert it normally
                    val parentClientPostId = metisDao.queryClientPostId(
                        serverId = host,
                        postId = post.post?.serverPostId ?: return@withTransaction null,
                        postingType = BasePostingEntity.PostingType.STANDALONE
                    ) ?: return@withTransaction null

                    insertOrUpdateAnswerPost(
                        isNewPost = true,
                        answerPostClientSidePostId = clientSidePostId,
                        answerPost = post,
                        metisDao = metisDao,
                        host = host,
                        parentPostClientSidePostId = parentClientPostId,
                        metisContext = metisContext,
                        answerPostId = null
                    )
                }
            }
        }
    }

    override suspend fun upgradeClientSidePost(
        host: String,
        metisContext: MetisContext,
        clientSidePostId: String,
        post: StandalonePost
    ) {
        val metisDao = databaseProvider.metisDao

        databaseProvider.database.withTransaction {
            val doesPostAlreadyExist = metisDao.isPostPresentInContext(
                serverId = host,
                serverPostId = post.id ?: return@withTransaction,
                courseId = metisContext.courseId,
                conversationId = metisContext.conversationId
            )

            // In rare cases, the websocket connection already inserted the post. In that case, we can delete the client side post
            if (doesPostAlreadyExist) {
                // instead of upgrading, we delete the client side post
                metisDao.deletePostingWithClientSideId(clientPostId = clientSidePostId)
                return@withTransaction
            }

            metisDao.upgradePost(
                clientSidePostId = clientSidePostId,
                serverSidePostId = post.id ?: return@withTransaction
            )

            insertOrUpdatePost(
                metisDao = metisDao,
                isNewPost = false,
                host = host,
                metisContext = metisContext,
                clientSidePostId = clientSidePostId,
                sp = post,
                isLiveCreated = false
            )
        }
    }

    override suspend fun upgradeClientSideAnswerPost(
        host: String,
        metisContext: MetisContext,
        clientSidePostId: String,
        post: AnswerPost
    ) {
        val metisDao = databaseProvider.metisDao

        databaseProvider.database.withTransaction {
            val doesPostAnswerAlreadyExist = metisDao.isPostPresentInContext(
                serverId = host,
                serverPostId = post.id ?: return@withTransaction,
                courseId = metisContext.courseId,
                conversationId = metisContext.conversationId
            )

            // In rare cases, the websocket connection already inserted the post answer. In that case, we can delete the client side post.
            if (doesPostAnswerAlreadyExist) {
                metisDao.deletePostingWithClientSideId(clientPostId = clientSidePostId)
                return@withTransaction
            }

            metisDao.upgradePost(
                clientSidePostId = clientSidePostId,
                serverSidePostId = post.id ?: return@withTransaction
            )

            val parentClientSidePostId = metisDao.queryClientPostId(
                host,
                post.post?.serverPostId ?: return@withTransaction,
                BasePostingEntity.PostingType.STANDALONE
            ) ?: return@withTransaction

            insertOrUpdateAnswerPost(
                isNewPost = false,
                answerPostClientSidePostId = clientSidePostId,
                answerPost = post,
                metisDao = metisDao,
                host = host,
                parentPostClientSidePostId = parentClientSidePostId,
                metisContext = metisContext,
                answerPostId = post.serverPostId
            )

            Log.d("AnswerDebug", "Upgrade finished")
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
                isNewPost = false,
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
            ) ?: insertOrUpdatePost(
                metisDao = metisDao, isNewPost = true,
                host = host,
                metisContext = metisContext,
                sp = post,
                isLiveCreated = true
            )
        }
    }

    /**
     * @return the client side post id or null if failed
     */
    private suspend fun insertOrUpdatePost(
        metisDao: MetisDao,
        isNewPost: Boolean,
        host: String,
        metisContext: MetisContext,
        clientSidePostId: String = UUID.randomUUID().toString(),
        sp: StandalonePost,
        isLiveCreated: Boolean
    ): String? {
        val postingAuthor = MetisUserEntity(
            serverId = host,
            id = sp.author?.id ?: return null,
            displayName = sp.author?.name ?: return null
        )

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

        // First insert the users as they have no dependencies
        metisDao.updateUsers(standalonePostReactionsUsers)
        metisDao.insertUsers(standalonePostReactionsUsers)

        metisDao.insertOrUpdateUser(postingAuthor)

        val standalonePostId = sp.id
        val postMetisContext =
            metisContext.toPostMetisContext(
                serverId = host,
                clientSidePostId = clientSidePostId,
                serverSidePostId = standalonePostId,
                postingType = BasePostingEntity.PostingType.STANDALONE
            )

        if (isNewPost) {
            metisDao.insertBasePost(standaloneBasePosting)
            metisDao.insertPost(standalonePosting)
            metisDao.insertReactions(standalonePostReactions)
            metisDao.insertTags(tags)

            metisDao.insertPostMetisContext(
                postMetisContext
            )
        } else {
            val isPostPresent = metisDao.isPostPresentInContext(
                clientSidePostId,
                standalonePostId,
                courseId = postMetisContext.courseId,
                conversationId = metisContext.conversationId
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
                host = host,
                standalonePostClientId = clientSidePostId,
                answerServerIds = sp.answers.orEmpty().mapNotNull { it.id }
            )
        }

        Log.d("AnswerDebug", "Updating answers for post ('${sp.content}') with id $clientSidePostId")


        for (ap in sp.answers.orEmpty()) {
            Log.d("AnswerDebug", "Inserting answer for post ('${sp.content}') with clientId $clientSidePostId and serverId ${ap.id}")
            val answerPostId = ap.id ?: continue

            val queryClientPostIdAnswer = metisDao.queryClientPostId(
                serverId = host,
                postId = answerPostId,
                postingType = BasePostingEntity.PostingType.ANSWER
            )

            insertOrUpdateAnswerPost(
                isNewPost = queryClientPostIdAnswer == null,
                answerPostClientSidePostId = queryClientPostIdAnswer ?: UUID.randomUUID()
                    .toString(),
                answerPost = ap,
                metisDao = metisDao,
                host = host,
                parentPostClientSidePostId = clientSidePostId,
                metisContext = metisContext,
                answerPostId = answerPostId
            )
        }

        return clientSidePostId
    }

    private suspend fun insertOrUpdateAnswerPost(
        isNewPost: Boolean,
        answerPostClientSidePostId: String,
        answerPost: AnswerPost,
        metisDao: MetisDao,
        host: String,
        parentPostClientSidePostId: String,
        metisContext: MetisContext,
        answerPostId: Long?
    ) {
        val authorRole = (if (isNewPost && answerPost.authorRole == null) {
            metisDao.queryPostAuthorRole(answerPostClientSidePostId)
        } else answerPost.authorRole?.asDb)?.asNetwork

        val (basePostingEntity, answerPostingEntity, metisUserEntity) = answerPost
            .copy(authorRole = authorRole)
            .asDb(
                serverId = host,
                clientSidePostId = answerPostClientSidePostId,
                parentClientSidePostId = parentPostClientSidePostId
            ) ?: return

        val answerPostReactionsWithUsers =
            answerPost.reactions.orEmpty().mapNotNull { it.asDb(host, answerPostClientSidePostId) }
        val answerPostReactions = answerPostReactionsWithUsers.map { it.first }
        val answerPostReactionUsers = answerPostReactionsWithUsers.map { it.second }

        metisDao.insertOrUpdateUser(metisUserEntity)
        metisDao.updateUsers(answerPostReactionUsers)
        metisDao.insertUsers(answerPostReactionUsers)

        if (isNewPost) {
            metisDao.insertBasePost(basePostingEntity)
            metisDao.insertAnswerPosting(answerPostingEntity)
            metisDao.insertReactions(answerPostReactions)
            metisDao.insertPostMetisContext(
                metisContext.toPostMetisContext(
                    serverId = host,
                    clientSidePostId = answerPostClientSidePostId,
                    serverSidePostId = answerPostId,
                    postingType = BasePostingEntity.PostingType.ANSWER
                )
            )
        } else {
            metisDao.updateBasePost(basePostingEntity)
            metisDao.updateAnswerPosting(answerPostingEntity)

            metisDao.updateReactions(answerPostClientSidePostId, answerPostReactions)
        }

        Log.d("AnswerDebug", "InsertOrUpdate client side post ('${answerPost.content}') with clientId " +
                "$answerPostClientSidePostId, serverId $answerPostId; new: $isNewPost")

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
        metisContext: MetisContext
    ): PagingSource<Int, PostPojo> {
        return databaseProvider.metisDao.queryCoursePosts(
            serverId = serverId,
            courseId = metisContext.courseId,
            conversationId = metisContext.conversationId
        )
    }

    override fun getLatestKnownPost(
        serverId: String,
        metisContext: MetisContext,
        allowClientSidePost: Boolean
    ): Flow<PostPojo?> {
        return databaseProvider.metisDao.queryLatestKnownPost(
            serverId = serverId,
            courseId = metisContext.courseId,
            conversationId = metisContext.conversationId,
            allowClientSidePost = allowClientSidePost
        )
    }

    override fun getStandalonePost(clientPostId: String): Flow<PostPojo?> {
        return databaseProvider.metisDao.queryStandalonePost(clientPostId)
    }

    override suspend fun getCachedPostCount(host: String, metisContext: MetisContext): Int {
        return databaseProvider.metisDao.queryContextPostCountNoLiveCreated(
            host,
            metisContext.courseId,
            metisContext.conversationId
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