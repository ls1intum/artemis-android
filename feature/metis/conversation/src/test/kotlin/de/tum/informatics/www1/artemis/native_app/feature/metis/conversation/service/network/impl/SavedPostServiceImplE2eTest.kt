package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ConversationMessagesBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.SavedPostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.model.SavedPostStatus
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@RunWith(RobolectricTestRunner::class)
@Category(EndToEndTest::class)
class SavedPostServiceImplTest : ConversationMessagesBaseTest() {

    private val sut: SavedPostService get() = get()

    @Test
    fun `test GIVEN the SavedPostService WHEN saving a post THEN calling getSavedPosts with status InProgress returns this saved post`() {
        runTest(timeout = DefaultTimeoutMillis.milliseconds) {
            val post = postDefaultMessage()

            sut.savePost(
                post = post,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not save post")

            val savedPosts = sut.getSavedPosts(
                status = SavedPostStatus.IN_PROGRESS,
                courseId = course.id!!,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not get saved posts")

            assertEquals(1, savedPosts.size)
            val receivedPost = savedPosts.first()
            assertEquals(post.id, receivedPost.id)
            assertEquals(post.isSaved, true)
        }
    }
}