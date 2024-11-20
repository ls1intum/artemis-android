package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

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

        // WHEN: Inserting a live-created post
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
        // GIVEN: An empty chat

        // WHEN: Inserting a live-created post
        sut.insertLiveCreatedPost(
            host = host,
            metisContext = metisContext,
            post = basePost
        )

        // THEN: The post should be inserted and matched to the correct conversation
        assertPostIsCreated()
        assertPostIsMatchedToConversation()
    }

    private fun assertPostIsCreated() {
        //TODO
    }

    private fun assertStoredContentIsNotModified() {
        //TODO
    }

    private fun assertPostIsMatchedToConversation() {
        //TODO
    }
}