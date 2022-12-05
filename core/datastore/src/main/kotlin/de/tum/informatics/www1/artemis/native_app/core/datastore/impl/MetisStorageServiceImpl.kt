package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import androidx.paging.PagingSource
import androidx.room.withTransaction
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.dao.MetisDao
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisFilter
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*
import de.tum.informatics.www1.artemis.native_app.core.model.metis.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * This implementation only displays live created posts, but ignores them when counting the posts for the next page request.
 */
class MetisStorageServiceImpl(
    private val databaseProvider: DatabaseProvider
) : MetisStorageService {

    companion object {
        private val BasePostingEntity.UserRole.asNetwork: UserRole
            get() = when (this) {
                BasePostingEntity.UserRole.INSTRUCTOR -> UserRole.INSTRUCTOR
                BasePostingEntity.UserRole.TUTOR -> UserRole.TUTOR
                BasePostingEntity.UserRole.USER -> UserRole.USER
            }

        private val UserRole.asDb: BasePostingEntity.UserRole
            get() = when (this) {
                UserRole.INSTRUCTOR -> BasePostingEntity.UserRole.INSTRUCTOR
                UserRole.TUTOR -> BasePostingEntity.UserRole.TUTOR
                UserRole.USER -> BasePostingEntity.UserRole.USER
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
                authorRole = authorRole?.asDb ?: BasePostingEntity.UserRole.USER
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
            val basePost = BasePostingEntity(
                postId = clientSidePostId,
                serverId = serverId,
                postingType = BasePostingEntity.PostingType.ANSWER,
                authorId = author?.id ?: return null,
                creationDate = creationDate ?: Instant.fromEpochSeconds(0),
                content = content,
                authorRole = authorRole?.asDb ?: BasePostingEntity.UserRole.USER
            )

            val answer = AnswerPostingEntity(
                postId = clientSidePostId,
                parentPostId = parentClientSidePostId,
                resolvesPost = resolvedPost
            )

            val user = MetisUserEntity(
                serverId = serverId,
                id = author?.id ?: return null,
                displayName = author?.name ?: "NULL"
            )

            return Triple(basePost, answer, user)
        }

        private fun Reaction.asDb(
            serverId: String,
            clientSidePostId: String
        ): Pair<PostReactionEntity, MetisUserEntity>? {
            return PostReactionEntity(
                serverId = serverId,
                postId = clientSidePostId,
                emojiId = emojiId ?: return null,
                authorId = user?.id ?: return null
            ) to MetisUserEntity(
                serverId = serverId,
                id = user?.id ?: return null,
                displayName = user?.name ?: return null
            )
        }

        private fun MetisContext.toPostMetisContext(
            serverId: String,
            clientSidePostId: String,
            serverSidePostId: Int
        ) =
            MetisPostContextEntity(
                serverId,
                courseId,
                exerciseId,
                lectureId,
                serverSidePostId,
                clientSidePostId
            )

        private val MetisContext.lectureId: Int get() = if (this is MetisContext.Lecture) lectureId else -1
        private val MetisContext.exerciseId: Int get() = if (this is MetisContext.Exercise) exerciseId else -1
    }

    override suspend fun insertOrUpdatePosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>,
        clearPreviousPosts: Boolean
    ) {
        val metisDao = databaseProvider.database.metisDao()

        databaseProvider.database.withTransaction {
            metisDao.clearAll(
                host,
                metisContext.courseId,
                metisContext.lectureId,
                metisContext.exerciseId
            )

            //Extract the db entities from the network entities. With the schema invalid entities are discarded.
            for (sp in posts) {
                val queryClientPostId = metisDao.queryClientPostId(
                    serverId = host,
                    courseId = metisContext.courseId,
                    exerciseId = metisContext.exerciseId,
                    lectureId = metisContext.lectureId,
                    postId = sp.id ?: continue
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
        val metisDao = databaseProvider.database.metisDao()
        databaseProvider.database.withTransaction {
            val clientSidePostId = metisDao.queryClientPostId(
                host,
                metisContext.courseId,
                metisContext.exerciseId,
                metisContext.lectureId,
                post.id ?: return@withTransaction
            ) ?: return@withTransaction

            //The author role seems to be omitted when sending an update. Therefore, we just load the one still cached.
            val actPost = if (post.authorRole == null) {
                val storedRole = when (metisDao.queryPostAuthorRole(clientSidePostId)) {
                    BasePostingEntity.UserRole.INSTRUCTOR -> UserRole.INSTRUCTOR
                    BasePostingEntity.UserRole.TUTOR -> UserRole.TUTOR
                    BasePostingEntity.UserRole.USER -> UserRole.USER
                }
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
    ) {
        val metisDao = databaseProvider.database.metisDao()
        databaseProvider.database.withTransaction {
            insertOrUpdatePost(metisDao, host, metisContext, null, post, true)
        }
    }

    private suspend fun insertOrUpdatePost(
        metisDao: MetisDao,
        host: String,
        metisContext: MetisContext,
        queryClientPostId: String?,
        sp: StandalonePost,
        isLiveCreated: Boolean
    ) {
        val postingAuthor = MetisUserEntity(
            serverId = host,
            id = sp.author?.id ?: return,
            displayName = sp.author?.name ?: return
        )

        val clientSidePostId: String = queryClientPostId ?: UUID.randomUUID().toString()

        val (standaloneBasePosting, standalonePosting) = sp.asDb(
            serverId = host,
            clientSidePostId = clientSidePostId,
            isLiveCreated = isLiveCreated
        ) ?: return

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
        if (queryClientPostId != null) {
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
            metisDao.insertPostMetisContext(
                metisContext.toPostMetisContext(host, clientSidePostId, sp.id ?: return)
            )
            metisDao.insertBasePost(standaloneBasePosting)
            metisDao.insertPost(standalonePosting)
            metisDao.insertReactions(standalonePostReactions)
            metisDao.insertTags(tags)
        }

        for (ap in sp.answers.orEmpty()) {
            val queryClientPostIdAnswer = metisDao.queryClientPostId(
                serverId = host,
                courseId = metisContext.courseId,
                exerciseId = metisContext.exerciseId,
                lectureId = metisContext.lectureId,
                postId = ap.id ?: return
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
                ) ?: return

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
                metisDao.insertPostMetisContext(
                    metisContext.toPostMetisContext(
                        host,
                        answerClientSidePostId,
                        ap.id ?: return
                    )
                )
                metisDao.insertBasePost(basePostingEntity)
                metisDao.insertAnswerPosting(answerPostingEntity)
                metisDao.insertReactions(answerPostReactions)
            }
        }
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

    override suspend fun deletePosts(host: String, postIds: List<Int>) {
        val metisDao = databaseProvider.database.metisDao()
        databaseProvider.database.withTransaction {
            metisDao.deletePostingsWithServerIds(host, postIds)
        }
    }

    override fun getStoredPosts(
        serverId: String,
        clientId: Int,
        filter: List<MetisFilter>,
        sortingStrategy: MetisSortingStrategy,
        query: String?,
        metisContext: MetisContext
    ): PagingSource<Int, Post> {
        return databaseProvider.database.metisDao().queryCoursePosts(
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
        return databaseProvider.database.metisDao().queryStandalonePost(clientPostId)
    }

    override suspend fun getStandalonePostMetisContext(clientPostId: String): MetisContext {
        val metisDao = databaseProvider.database.metisDao()
        val entity = metisDao.queryMetisContextForStandalonePost(clientPostId)

        return when {
            entity.exerciseId != -1 -> MetisContext.Exercise(entity.courseId, entity.exerciseId)
            entity.lectureId != -1 -> MetisContext.Lecture(entity.courseId, entity.lectureId)
            else -> MetisContext.Course(entity.courseId)
        }
    }

    override suspend fun getCachedPostCount(host: String, metisContext: MetisContext): Int {
        return databaseProvider.database.metisDao().queryContextPostCountNoLiveCreated(
            host,
            metisContext.courseId,
            metisContext.exerciseId,
            metisContext.lectureId
        )
    }
}