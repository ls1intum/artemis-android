package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.impl

import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ConversationMessagesBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.SavedPostsTestUtil
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.SavedPostStatus
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@Ignore("Suddenly started failing: https://github.com/ls1intum/artemis-android/issues/370")
@RunWith(RobolectricTestRunner::class)
@Category(EndToEndTest::class)
class SavedPostServiceImplTest : ConversationMessagesBaseTest() {

    private val sut: de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.service.SavedPostService get() = get()

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
            assertEquals(true, receivedPost.isSaved)
        }
    }

    @Test
    fun `test GIVEN the SavedPostService WHEN deleting a saved post THEN calling getSavedPosts with status InProgress returns no saved post`() {
        runTest(timeout = DefaultTimeoutMillis.milliseconds) {
            val post = postDefaultMessage()

            sut.savePost(
                post = post,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not save post")

            sut.deleteSavedPost(
                post = post,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not remove saved post")

            val savedPosts = sut.getSavedPosts(
                status = SavedPostStatus.IN_PROGRESS,
                courseId = course.id!!,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not get saved posts")

            assertEquals(0, savedPosts.size)
        }
    }

    @Test
    fun `test GIVEN the SavedPostService WHEN changing the status of a saved post THEN calling getSavedPosts with status Done returns this saved post`() {
        runTest(timeout = DefaultTimeoutMillis.milliseconds) {
            val post = postDefaultMessage()

            sut.savePost(
                post = post,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not save post")

            sut.changeSavedPostStatus(
                post = de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts.SavedPostsTestUtil.fromStandalonePost(post),
                status = SavedPostStatus.COMPLETED,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not change status of saved post")

            val savedPosts = sut.getSavedPosts(
                status = SavedPostStatus.COMPLETED,
                courseId = course.id!!,
                authToken = accessToken,
                serverUrl = testServerUrl
            )
                .orThrow("Could not get saved posts")

            assertEquals(1, savedPosts.size)
            val receivedPost = savedPosts.first()
            assertEquals(post.id, receivedPost.id)
            assertEquals(true, receivedPost.isSaved)
            assertEquals(SavedPostStatus.COMPLETED, receivedPost.savedPostStatus)
        }
    }
}