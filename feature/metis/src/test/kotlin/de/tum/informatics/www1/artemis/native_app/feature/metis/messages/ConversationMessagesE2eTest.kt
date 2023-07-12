package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.Post
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.TEST_TAG_METIS_MODIFICATION_FAILURE_DIALOG
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.TEST_TAG_POST_CONTEXT_BOTTOM_SHEET
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.predefinedEmojiIds
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.TEST_TAG_METIS_POST_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.testTagForPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.TEST_TAG_CAN_CREATE_REPLY
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.TEST_TAG_REPLY_SEND_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ProvideLocalVisibleMetisContextManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationMessagesE2eTest : ConversationMessagesBaseTest() {

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `shows existing messages`() {
        val posts = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                (0 until 3).map { createPost("post content $it") }
                    .map {
                        metisModificationService.createPost(
                            context = metisContext,
                            post = it,
                            serverUrl = testServerUrl,
                            authToken = accessToken
                        )
                            .orThrow("Could not create post $it")
                    }
            }
        }

        setupUiAndViewModel()

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_METIS_POST_LIST),
            DefaultTimeoutMillis
        )

        posts.forEach { post ->
            composeTestRule.waitUntilExactlyOneExists(hasTestTag(testTagForPost(post.serverPostId)))

            composeTestRule
                .onNodeWithTag(testTagForPost(post.serverPostId))
                .performScrollTo()
                .assertExists("Post $post does not exist")
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can send new message`() {
        val text = "test message"
        runTest(timeout = DefaultTimeoutMillis.milliseconds * 4) {
            metisModificationService.createPost(metisContext, createPost(text), testServerUrl, accessToken)

            assertTrue(
                metisService.getPosts(
                    MetisService.StandalonePostsContext(
                        metisContext,
                        emptyList(),
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
        val (viewModel, post) = setupUiAndViewModelWithPost()

        openPostBottomSheet(post.id!!)

        composeTestRule
            .onNodeWithText(context.getString(R.string.post_edit))
            .performClick()

        composeTestRule
            .onNode(replyTextFieldMatcher)
            .performTextClearance()

        val newText = "updated post text"

        composeTestRule
            .onNode(replyTextFieldMatcher)
            .performTextInput(newText)

        composeTestRule
            .onNodeWithTag(TEST_TAG_REPLY_SEND_BUTTON)
            .performClick()

        testDispatcher.scheduler.advanceUntilIdle()

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_CAN_CREATE_REPLY),
            DefaultTimeoutMillis
        )

        composeTestRule
            .onNodeWithTag(TEST_TAG_METIS_MODIFICATION_FAILURE_DIALOG)
            .assertDoesNotExist()

        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                viewModel.forceReload().join()
            }
        }

        composeTestRule
            .waitUntilExactlyOneExists(
                hasTestTag(testTagForPost(post.id!!)) and hasAnyDescendant(hasText(newText)),
                DefaultTimeoutMillis
            )
    }

    private fun setupUiAndViewModelWithPost(additionalSetup: suspend (StandalonePost) -> Unit = {}): Pair<MetisListViewModel, StandalonePost> {
        val post = postDefaultMessage(additionalSetup)

        val postTestTag = testTagForPost(post.id!!)

        val viewModel = setupUiAndViewModel()

        // Wait until post exists
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(postTestTag),
            DefaultTimeoutMillis
        )

        return viewModel to post
    }

    private fun openPostBottomSheet(postId: Long) {
        composeTestRule
            .onNode(hasTestTag(testTagForPost(postId)) and hasClickAction())
            .performTouchInput { longClick(Offset(0f, 0f)) }
    }

    @OptIn(KoinInternalApi::class)
    private fun setupUiAndViewModel(): MetisListViewModel {
        val viewModel = MetisListViewModel(
            initialMetisContext = metisContext,
            metisService = get(),
            metisStorageService = get(),
            serverConfigurationService = get(),
            metisContextManager = get(),
            metisModificationService = get(),
            accountService = get(),
            websocketProvider = get(),
            accountDataService = get(),
            networkStatusProvider = get(),
            conversationService = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                ProvideLocalVisibleMetisContextManager(visibleMetisContextManager = EmptyVisibleMetisContextManager) {
                    MetisChatList(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = viewModel,
                        listContentPadding = PaddingValues(),
                        onClickViewPost = {},
                    )
                }
            }
        }

        testDispatcher.scheduler.advanceTimeBy(30.seconds)

        return viewModel
    }
}