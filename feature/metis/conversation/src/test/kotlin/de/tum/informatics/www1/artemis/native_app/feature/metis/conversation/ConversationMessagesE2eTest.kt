package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions.predefinedEmojiIds
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisFilter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metistest.ConversationMessagesBaseTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.Logger
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationMessagesE2eTest : ConversationMessagesBaseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows existing messages`() {
        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            val posts = (0 until 3).map { createPost("post content $it") }
                .map {
                    metisModificationService.createPost(
                        context = metisContext,
                        post = it,
                        serverUrl = testServerUrl,
                        authToken = accessToken
                    )
                        .orThrow("Could not create post $it")
                }

            Logger.info("Created posts are $posts")

            val downloadedPosts = metisService.getPosts(
                MetisService.StandalonePostsContext(
                    metisContext,
                    MetisFilter.ALL,
                    "",
                    MetisSortingStrategy.DATE_ASCENDING,
                    null
                ),
                20,
                0,
                accessToken,
                testServerUrl
            ).orThrow("Posts could not be downloaded")

            Logger.info("Downloaded posts are $downloadedPosts")

            posts.forEach { createdPost ->
                assertTrue(
                    downloadedPosts.any { it.id == createdPost.id },
                    "Created post $createdPost was not found in downloaded tests"
                )
            }
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can send new message`() {
        val text = "test message"
        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            metisModificationService.createPost(
                metisContext,
                createPost(text),
                testServerUrl,
                accessToken
            )

            assertTrue(
                metisService.getPosts(
                    MetisService.StandalonePostsContext(
                        metisContext,
                        MetisFilter.ALL,
                        null,
                        MetisSortingStrategy.DATE_DESCENDING,
                        null
                    ),
                    20,
                    0,
                    accessToken,
                    testServerUrl
                ).orThrow("Could not load posts")
                    .any { it.content == text },
                "Could not find created message"
            )
        }
    }

    /**
     * This test as a UI test was too flaky, so for now a regular test without UI
     */
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can react to message with emoji`() {
        val emojiId = predefinedEmojiIds.first()

        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            val post = postDefaultMessage()

            metisModificationService.createReaction(
                metisContext,
                MetisModificationService.AffectedPost.Standalone(post.id!!),
                emojiId,
                testServerUrl,
                accessToken
            ).orThrow("Could not create reaction")

            val updatedPost = metisService
                .getPost(metisContext, post.id!!, testServerUrl, accessToken)
                .orThrow("Could not get updated post")

            assertTrue(
                updatedPost.reactions.orEmpty().any { it.emojiId == emojiId },
                "No reaction with emojiId=$emojiId was found in $updatedPost"
            )
        }
    }

    /**
     * This test as a UI test was too flaky, so for now a regular test without UI
     */
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can delete existing reaction`() {
        val emojiId = predefinedEmojiIds.first()

        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            val post = postDefaultMessage { post ->
                metisModificationService.createReaction(
                    context = metisContext,
                    post = MetisModificationService.AffectedPost.Standalone(post.id!!),
                    emojiId = emojiId,
                    serverUrl = testServerUrl,
                    authToken = accessToken
                ).orThrow("Could not create reaction")
            }

            val newPost =
                metisService.getPost(metisContext, post.id!!, testServerUrl, accessToken)
                    .orThrow("Could not load new post")

            val reaction = newPost.reactions.orEmpty().first { it.emojiId == emojiId }

            assertTrue(
                metisModificationService.deleteReaction(
                    metisContext,
                    reaction.id!!,
                    testServerUrl,
                    accessToken
                ).or(false),
                "Could not delete reaction"
            )

            val finalPost =
                metisService.getPost(metisContext, post.id!!, testServerUrl, accessToken)
                    .orThrow("Could not load final post")

            assertNull(
                finalPost.reactions.orEmpty().firstOrNull { it.emojiId == emojiId },
                "Emoji id still present"
            )
        }
    }

    /**
     * This test as a UI test was too flaky, so for now a regular test without UI
     */
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can delete message`() {
        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            val post = postDefaultMessage()

            metisModificationService.deletePost(
                metisContext,
                MetisModificationService.AffectedPost.Standalone(post.id!!),
                testServerUrl,
                accessToken
            )

            assertIs<NetworkResponse.Failure<StandalonePost>>(
                metisService.getPost(
                    metisContext,
                    post.id!!,
                    testServerUrl,
                    accessToken
                )
            )
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can edit message`() {
        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            val basePost = metisModificationService.createPost(
                context = metisContext,
                post = createPost("test message"),
                serverUrl = testServerUrl,
                authToken = accessToken
            ).orThrow("Could not create message")

            val newText = "updated message"

            metisModificationService.updateStandalonePost(
                metisContext,
                basePost.copy(content = newText),
                testServerUrl,
                accessToken
            ).orThrow("Could not edit message")

            val editedPost = metisService
                .getPost(metisContext, basePost.serverPostId!!, testServerUrl, accessToken)
                .orThrow("Could not download edited post")

            assertEquals(newText, editedPost.content, "Edited post does not have the updated text content")
        }
    }

    @Test
    fun `can pin message`() {
        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            val basePost = postDefaultMessage()

            metisModificationService.updatePostDisplayPriority(
                metisContext,
                basePost.copy(displayPriority = DisplayPriority.PINNED),
                testServerUrl,
                accessToken
            ).orThrow("Could not pin message")

            val editedPost = metisService
                .getPost(metisContext, basePost.serverPostId!!, testServerUrl, accessToken)
                .orThrow("Could not download pinned post")

            assertEquals(DisplayPriority.PINNED, editedPost.displayPriority, "Edited post does not have the updated display priority")
        }
    }
}