package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.asSnapshot
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metistest.MetisDatabaseProviderMock
import kotlinx.datetime.Clock

abstract class MetisStorageBaseTest {

    private val databaseProviderMock = MetisDatabaseProviderMock(InstrumentationRegistry.getInstrumentation().context)
    internal val sut = MetisStorageServiceImpl(databaseProviderMock)

    internal val host = "host"

    private val author = User(id = 20, name = "AuthorName")
    private val parentClientPostId = "parent-client-id-0"
    internal val answerClientPostId = "answer-client-id-0"

    private val course: MetisContext.Course = MetisContext.Course(courseId = 1)
    internal val conversation = OneToOneChat(id = 2)
    internal val metisContext = MetisContext.Conversation(course.courseId, conversation.id)

    internal val conversationTwo = OneToOneChat(id = 3)
    internal val metisContextTwo = MetisContext.Conversation(course.courseId, conversationTwo.id)

    internal val localAnswerPojo = AnswerPostPojo(
        parentPostId = parentClientPostId,
        postId = answerClientPostId,
        resolvesPost = false,
        basePostingCache = AnswerPostPojo.BasePostingCache(
            serverPostId = 0,
            authorId = author.id,
            creationDate = Clock.System.now(),
            updatedDate = null,
            content = "Answer post content 0",
            authorRole = UserRole.USER,
            authorName = author.name!!
        ),
        reactions = emptyList(),
        serverPostIdCache = AnswerPostPojo.ServerPostIdCache(
            serverPostId = null     // Only local answer post, no server id
        )
    )

    internal val basePostPojo = PostPojo(
        clientPostId = parentClientPostId,
        serverPostId = 0,
        content = "Base post content",
        resolved = false,
        updatedDate = null,
        creationDate = Clock.System.now(),
        authorId = author.id,
        title = null,
        authorName = author.name!!,
        authorRole = UserRole.USER,
        courseWideContext = null,
        tags = emptyList(),
        answers = emptyList(),
        reactions = emptyList()
    )

    internal val basePost = StandalonePost(basePostPojo, conversation)
    internal val basePostTwo = StandalonePost(basePostPojo, conversationTwo)
    internal val localAnswer = AnswerPost(localAnswerPojo, basePost)

    internal suspend fun getStoredPosts(metisContext: MetisContext) = sut.getStoredPosts(
        serverId = host,
        metisContext = metisContext
    ).loadAsList()

    internal suspend fun <T : Any> PagingSource<Int, T>.loadAsList(): List<T> {
        return Pager(PagingConfig(pageSize = 10), pagingSourceFactory = { this }).flow.asSnapshot {
            scrollTo(50)
        }
    }
}