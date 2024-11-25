package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class MetisStorageServiceTestLiveCreation : MetisStorageBaseTest() {

    @Test
    fun testInsertLiveCreatedPostWithExistingPost() = runTest {
        // GIVEN: A base post already exists
        sut.insertOrUpdatePosts(
            host = host,
            metisContext = metisContext,
            posts = listOf(basePost),
        )

        // WHEN: Inserting a live-created post with the same id into the same conversation
        sut.insertLiveCreatedPost(
            host = host,
            metisContext = metisContext,
            post = basePost
        )

        // THEN: No post should be inserted and the existing posts should not be modified
        assertStoredContentIsNotModified()
    }

    @Test
    fun testInsertLiveCreatedPost() = runTest {
        // GIVEN: An empty conversation

        // WHEN: Inserting a live-created post into a different conversation than the current
        sut.insertLiveCreatedPost(
            host = host,
            metisContext = metisContext,
            post = basePostTwo
        )

        // THEN: The post should be inserted and matched to the correct conversation
        val createdPost = assertPostIsCreated()
        assertPostIsMatchedToConversation(createdPost)
    }

    private suspend fun assertPostIsCreated(): PostPojo {
        val posts = getStoredPosts(metisContextTwo)
        assertEquals(1, posts.size)
        assertEquals(basePostTwo.content, posts.first().content)
        return posts[0]
    }

    private suspend fun assertStoredContentIsNotModified() {
        val posts = getStoredPosts(metisContext)
        val standalonePost = StandalonePost(posts.first(), conversation)
        assertEquals(1, posts.size)
        assertEquals(basePost.content, standalonePost.content)
        assertEquals(basePost.id, standalonePost.id)
        assertEquals(basePost.conversation, standalonePost.conversation)
    }

    private fun assertPostIsMatchedToConversation(createdPost: PostPojo) {
        val createdStandalonePost = StandalonePost(createdPost, conversationTwo)
        assertEquals(basePostTwo.conversation?.id, createdStandalonePost.conversation?.id)
    }
}