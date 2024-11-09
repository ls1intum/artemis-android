package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import androidx.paging.PagingSource
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metistest.MetisDatabaseProviderMock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class MetisStorageServiceImplTest {

    private val databaseProviderMock = MetisDatabaseProviderMock(InstrumentationRegistry.getInstrumentation().context)
    private val sut = MetisStorageServiceImpl(databaseProviderMock)

    private val host = "host"

    private val author = User(id = 20, name = "AuthorName")
    private val parentClientPostId = "parent-client-id-0"
    private val answerClientPostId = "answer-client-id-0"

    private val course: MetisContext.Course = MetisContext.Course(courseId = 1)
    private val conversation = OneToOneChat(id = 2)
    private val metisContext = MetisContext.Conversation(course.courseId, conversation.id)

    private val localAnswerPojo = AnswerPostPojo(
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

    private val basePostPojo = PostPojo(
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

    private val basePost = StandalonePost(basePostPojo, conversation)
    private val localAnswer = AnswerPost(localAnswerPojo, basePost)

    @Test
    fun testInsertClientSidePost() = runTest {
        // GIVEN: A base post
        sut.insertOrUpdatePosts(
            host = host,
            metisContext = metisContext,
            posts = listOf(basePost),
        )

        // WHEN: Inserting a client side answer post
        sut.insertClientSidePost(
            host = host,
            metisContext = metisContext,
            post = localAnswer,
            clientSidePostId = answerClientPostId
        )

        // THEN: Both the base post and the answer post are stored
        val posts = sut.getStoredPosts(
            serverId = host,
            metisContext = metisContext
        ).loadAsList()

        assertEquals(1, posts.size)
        assertEquals(1, posts.first().answers.size)
    }


    @Test
    fun testUpgradeClientSideAnswerPost() = runTest {
        // GIVEN: A post with a new only local answer post
        sut.insertOrUpdatePosts(
            host = host,
            metisContext = metisContext,
            posts = listOf(basePost)
        )
        sut.insertClientSidePost(
            host = host,
            metisContext = metisContext,
            clientSidePostId = answerClientPostId,
            post = localAnswer
        )

        // WHEN: insertOrUpdatePosts is called before upgradeClientSideAnswerPost.
        val answerPojoUpdated = localAnswerPojo.copy(serverPostIdCache = localAnswerPojo.serverPostIdCache.copy(serverPostId = 1))
        var basePostUpdated = StandalonePost(basePostPojo, conversation)
        val answerUpdated = AnswerPost(answerPojoUpdated, basePostUpdated)
        basePostUpdated = basePostUpdated.copy(answers = listOf(answerUpdated))

        // Called by the WebSocket
        sut.updatePost(
            host = host,
            metisContext = metisContext,
            post = basePostUpdated
        )

        // Called by SendConversationPostWorker
        sut.upgradeClientSideAnswerPost(
            host = host,
            metisContext = metisContext,
            clientSidePostId = answerClientPostId,
            post = answerUpdated
        )

        // THEN: Only one answer post is stored
        val posts = sut.getStoredPosts(
            serverId = host,
            metisContext = metisContext
        ).loadAsList()
        assertEquals(1, posts.size)
        assertEquals(1, posts.first().answers.size)
    }


    private suspend fun <T : Any>PagingSource<Int, T>.loadAsList(): List<T> {
        val result = mutableListOf<T>()
        var key = 0
        do {
            val loadResult = load(PagingSource.LoadParams.Refresh(key, 10, false))
            if (loadResult is PagingSource.LoadResult.Page) {
                result.addAll(loadResult.data)
                key = loadResult.nextKey ?: break
            }
        } while (true)
        return result
    }
}