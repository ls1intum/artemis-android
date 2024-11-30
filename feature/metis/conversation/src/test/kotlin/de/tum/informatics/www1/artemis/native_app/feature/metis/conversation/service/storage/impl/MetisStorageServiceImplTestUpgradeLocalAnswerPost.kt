package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl


import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class MetisStorageServiceImplUpgradeLocalAnswerPostTest : MetisStorageBaseTest() {

    private lateinit var basePostUpdated: StandalonePost
    private lateinit var answerUpdated: AnswerPost

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
        assertStoredContentIsTheSame()
    }

    @Test
    fun testUpgradeClientSideAnswerPost() = runTest {
        // GIVEN: A post with a new only local answer post
        setupPostWithLocalAnswer()

        // WHEN: insertOrUpdatePosts is called before upgradeClientSideAnswerPost.
        updateAnswerPostWithServerId()

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

        // THEN: Content stays the same and the upgrade is successful
        assertStoredContentIsTheSame()
        assertUpgradeSuccessful()
    }

    @Test
    fun testUpgradeClientSideAnswerPost2() = runTest {
        // GIVEN: A post with a new only local answer post
        setupPostWithLocalAnswer()

        // WHEN: upgradeClientSideAnswerPost is called before updatePost.
        updateAnswerPostWithServerId()

        // Called by SendConversationPostWorker
        sut.upgradeClientSideAnswerPost(
            host = host,
            metisContext = metisContext,
            clientSidePostId = answerClientPostId,
            post = answerUpdated
        )

        // Called by the WebSocket
        sut.updatePost(
            host = host,
            metisContext = metisContext,
            post = basePostUpdated
        )

        // THEN: Content stays the same and the upgrade is successful
        assertStoredContentIsTheSame()
        assertUpgradeSuccessful()
    }

    private suspend fun setupPostWithLocalAnswer() {
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
    }


    private fun updateAnswerPostWithServerId() {
        val answerPojoUpdated = localAnswerPojo.copy(serverPostIdCache = localAnswerPojo.serverPostIdCache.copy(serverPostId = 1))
        basePostUpdated = StandalonePost(basePostPojo, conversation)
        answerUpdated = AnswerPost(answerPojoUpdated, basePostUpdated)
        basePostUpdated = basePostUpdated.copy(answers = listOf(answerUpdated))
    }

    private suspend fun assertStoredContentIsTheSame() {
        val posts = getStoredPosts(metisContext)
        assertEquals(1, posts.size)
        assertEquals(basePostPojo.content, posts.first().content)
        assertEquals(1, posts.first().answers.size)
        assertEquals(localAnswerPojo.content, posts.first().answers.first().content)
    }

    private suspend fun assertUpgradeSuccessful() {
        val posts = getStoredPosts(metisContext)
        assertEquals(answerUpdated.serverPostId, posts.first().answers.first().serverPostId)
    }
}