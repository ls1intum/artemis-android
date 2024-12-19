package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class MetisStorageServiceTestUpdatePost : MetisStorageBaseTest() {

    @Test
    fun testUpdatePostWithSameClientPostId() = runTest {
        // GIVEN: Two posts in two different conversations with the same clientPostId
        val post1 = basePost
        val post2 = basePostTwo
        sut.insertOrUpdatePosts(
            host = host,
            metisContext = metisContext,
            posts = listOf(post1, post2),
        )

        // WHEN: Updating post1 while the MetisContext of post2 is active
        sut.updatePost(
            host = host,
            metisContext = metisContextTwo,
            post = post1
        )

        // THEN: The previous call should not crash and this assertion should be reached
        assert(true)
    }
}