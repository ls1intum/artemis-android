package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import androidx.paging.PagingSource
import de.tum.informatics.www1.artemis.native_app.core.datastore.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import de.tum.informatics.www1.artemis.native_app.core.datastore.room.model.metis.*
import de.tum.informatics.www1.artemis.native_app.core.model.metis.*

class MetisStorageServiceImpl(
    private val databaseProvider: DatabaseProvider
) : MetisStorageService {

    companion object {
        private val UserRole.asStorage: PostingEntity.UserRole
            get() = when (this) {
                UserRole.INSTRUCTOR -> PostingEntity.UserRole.INSTRUCTOR
                UserRole.TUTOR -> PostingEntity.UserRole.TUTOR
                UserRole.USER -> PostingEntity.UserRole.USER
            }

        private val CourseWideContext.asStorage: PostingEntity.CourseWideContext
            get() = when (this) {
                CourseWideContext.TECH_SUPPORT -> PostingEntity.CourseWideContext.TECH_SUPPORT
                CourseWideContext.ORGANIZATION -> PostingEntity.CourseWideContext.ORGANIZATION
                CourseWideContext.RANDOM -> PostingEntity.CourseWideContext.RANDOM
                CourseWideContext.ANNOUNCEMENT -> PostingEntity.CourseWideContext.ANNOUNCEMENT
            }

        private val DisplayPriority.asStorage: PostingEntity.DisplayPriority
            get() = when (this) {
                DisplayPriority.PINNED -> PostingEntity.DisplayPriority.PINNED
                DisplayPriority.ARCHIVED -> PostingEntity.DisplayPriority.ARCHIVED
                DisplayPriority.NONE -> PostingEntity.DisplayPriority.NONE
            }

        private fun Reaction.asStorage(
            host: String,
            postId: Int,
            courseId: Int,
            exerciseId: Int,
            lectureId: Int
        ): Pair<PostReaction, MetisUserEntity>? {
            return PostReaction(
                serverId = host,
                postId = postId,
                emojiId = emojiId ?: return null,
                authorId = user?.id ?: return null,
                courseId = courseId,
                exerciseId = exerciseId,
                lectureId = lectureId
            ) to MetisUserEntity(
                serverId = host,
                id = user?.id ?: return null,
                displayName = user?.name ?: return null
            )
        }

        private val MetisContext.lectureId: Int get() = if (this is MetisContext.Lecture) lectureId else -1
        private val MetisContext.exerciseId: Int get() = if (this is MetisContext.Exercise) exerciseId else -1
    }

    override suspend fun insertOrUpdatePosts(
        host: String,
        metisContext: MetisContext,
        posts: List<StandalonePost>
    ) {
        data class DbModel(
            val posting: StandalonePosting,
            val basePostings: List<PostingEntity>,
            val answers: List<AnswerPosting>,
            val reactions: List<PostReaction>,
            val tags: List<StandalonePostTag>,
            val users: List<MetisUserEntity>
        )

        //Extract the db entities from the network entities. With the schema invalid entities are discarded.
        val modelPosts = posts.map { sp ->
            val postId = sp.id ?: return@map null
            val exerciseId = metisContext.exerciseId
            val lectureId = metisContext.lectureId

            val postingAuthor = MetisUserEntity(
                serverId = host,
                id = sp.author?.id ?: return@map null,
                displayName = sp.author?.name ?: return@map null
            )

            val standaloneBasePosting = PostingEntity(
                serverId = host,
                id = postId,
                courseId = metisContext.courseId,
                exerciseId = exerciseId,
                lectureId = lectureId,
                postingType = PostingEntity.PostingType.STANDALONE,
                authorId = postingAuthor.id,
                creationDate = sp.creationDate ?: return@map null,
                content = sp.content,
                authorRole = sp.authorRole?.asStorage ?: return@map null
            )

            val standalonePosting = StandalonePosting(
                serverId = host,
                postId = postId,
                courseId = metisContext.courseId,
                exerciseId = exerciseId,
                lectureId = lectureId,
                title = sp.title,
                context = sp.courseWideContext?.asStorage,
                displayPriority = sp.displayPriority?.asStorage,
                resolved = sp.resolved ?: false
            )

            val answers = sp.answers.orEmpty().map answerMap@{ ap ->
                val answerPostId = ap.id ?: return@answerMap null
                val authorId = ap.author?.id ?: return@answerMap null

                val basePost = PostingEntity(
                    id = answerPostId,
                    serverId = host,
                    courseId = metisContext.courseId,
                    exerciseId = exerciseId,
                    lectureId = lectureId,
                    postingType = PostingEntity.PostingType.ANSWER,
                    authorId = authorId,
                    creationDate = ap.creationDate ?: return@answerMap null,
                    content = ap.content,
                    authorRole = ap.authorRole?.asStorage ?: return@answerMap null
                )

                val answer = AnswerPosting(
                    serverId = host,
                    postId = answerPostId,
                    courseId = metisContext.courseId,
                    exerciseId = exerciseId,
                    lectureId = lectureId,
                    parentPostId = standalonePosting.postId,
                    resolvesPost = ap.resolvedPost
                )
                val user = MetisUserEntity(
                    serverId = host,
                    id = authorId,
                    displayName = ap.author?.name ?: return@answerMap null
                )

                Triple(basePost, answer, user)
            }.filterNotNull()

            val reactions: List<Pair<PostReaction, MetisUserEntity>> =
                sp.reactions.orEmpty().map { reaction ->
                    reaction.asStorage(host, postId, metisContext.courseId, exerciseId, lectureId)
                }.filterNotNull() + sp.answers.orEmpty().flatMap { answer ->
                    val answerPostId = answer.id ?: return@flatMap emptyList()
                    answer.reactions.orEmpty().map reactionMap@{ reaction ->
                        reaction.asStorage(host, answerPostId, metisContext.courseId, exerciseId, lectureId)
                    }
                }.filterNotNull()

            val tags = sp.tags.orEmpty().map tagMap@{ tag ->
                StandalonePostTag(
                    serverId = host,
                    postId = standalonePosting.postId,
                    tag = tag,
                    courseId = metisContext.courseId,
                    exerciseId = exerciseId,
                    lectureId = lectureId
                )
            }

            val users = answers.map { it.third } + reactions.map { it.second } + postingAuthor
            val basePostings = answers.map { it.first } + standaloneBasePosting

            DbModel(
                posting = standalonePosting,
                basePostings = basePostings,
                answers = answers.map { it.second },
                reactions = reactions.map { it.first },
                tags = tags,
                users = users
            )
        }.filterNotNull()

        databaseProvider.database.metisDao().insertOrUpdatePosts(
            basePostings = modelPosts.flatMap { it.basePostings },
            standalonePosts = modelPosts.map { it.posting },
            answerPosts = modelPosts.flatMap { it.answers },
            reactions = modelPosts.flatMap { it.reactions },
            tags = modelPosts.flatMap { it.tags },
            users = modelPosts.flatMap { it.users }
        )
    }

    override suspend fun deletePosts(host: String, metisContext: MetisContext, postIds: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun getStoredPosts(
        host: String,
        metisContext: MetisContext
    ): PagingSource<Int, Post> {
        return databaseProvider.database.metisDao().queryPosts(
            host,
            metisContext.courseId,
            metisContext.exerciseId,
            metisContext.lectureId,
            ""
        )
    }
}