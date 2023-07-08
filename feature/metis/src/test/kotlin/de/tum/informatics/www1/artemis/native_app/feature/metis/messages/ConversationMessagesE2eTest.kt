package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ExperimentalTestApi
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
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.TEST_TAG_POST_CONTEXT_BOTTOM_SHEET
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post.predefinedEmojiIds
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.TEST_TAG_METIS_POST_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.testTagForPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.TEST_TAG_REPLY_SEND_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ProvideLocalVisibleMetisContextManager
import kotlinx.coroutines.runBlocking
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

    fun `can send new message`() {
        val viewModel = setupUiAndViewModel()

        canSendTestImpl(
            "test message",
            TEST_TAG_METIS_POST_LIST
        ) {
            viewModel.forceReload()

            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can react to message with emoji`() {
        val post = postDefaultMessage()

        setupUiAndViewModel()

        val postTestTag = testTagForPost(post.id!!)

        // Wait until post exists
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(postTestTag),
            DefaultTimeoutMillis
        )

        openPostBottomSheet(post.id!!)

        // Wait for bottom sheet to appear
        composeTestRule
            .waitUntilExactlyOneExists(
                hasTestTag(TEST_TAG_POST_CONTEXT_BOTTOM_SHEET),
                DefaultTimeoutMillis
            )

        val emojiToReactWithId = predefinedEmojiIds.first()
        val emojiService: EmojiService = get()

        val emojiText = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                emojiService.emojiIdToUnicode(emojiToReactWithId)
            }
        }

        composeTestRule
            .onNode(
                hasAnyAncestor(hasTestTag(TEST_TAG_POST_CONTEXT_BOTTOM_SHEET)) and hasText(
                    emojiText
                )
            )
            .performClick()

        composeTestRule
            .waitUntilExactlyOneExists(
                hasTestTag(postTestTag) and hasAnyDescendant(
                    hasText(emojiText)
                ),
                DefaultTimeoutMillis
            )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can delete existing reaction`() {
        val emojiId = predefinedEmojiIds.first()

        val post = setupUiAndViewModelWithPost { post ->
            metisModificationService.createReaction(
                context = metisContext,
                post = MetisModificationService.AffectedPost.Standalone(post.id!!),
                emojiId = emojiId,
                serverUrl = testServerUrl,
                authToken = accessToken
            ).orThrow("Could not create reaction")
        }

        val emojiService: EmojiService = get()

        val emojiText = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                emojiService.emojiIdToUnicode(emojiId)
            }
        }

        composeTestRule
            .onNode(hasAnyAncestor(hasTestTag(testTagForPost(post.id!!))) and hasText(emojiText))
            .performClick()

        composeTestRule
            .waitUntilDoesNotExist(
                hasAnyAncestor(hasTestTag(testTagForPost(post.id!!))) and hasText(emojiText),
                DefaultTimeoutMillis
            )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can delete message`() {
        val post = setupUiAndViewModelWithPost()

        openPostBottomSheet(post.id!!)

        composeTestRule
            .onNodeWithText(context.getString(R.string.post_delete))
            .performClick()

        composeTestRule
            .waitUntilDoesNotExist(
                hasTestTag(testTagForPost(post.id!!)),
                DefaultTimeoutMillis
            )
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can edit message`() {
        val post = setupUiAndViewModelWithPost()

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

        composeTestRule
            .waitUntilExactlyOneExists(
                hasTestTag(testTagForPost(post.id!!)) and hasAnyDescendant(hasText(newText)),
                DefaultTimeoutMillis
            )
    }

    private fun setupUiAndViewModelWithPost(additionalSetup: suspend (StandalonePost) -> Unit = {}): StandalonePost {
        val post = postDefaultMessage(additionalSetup)

        val postTestTag = testTagForPost(post.id!!)

        setupUiAndViewModel()

        // Wait until post exists
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(postTestTag),
            DefaultTimeoutMillis
        )

        return post
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
                LocalKoinScope provides KoinPlatformTools.defaultContext().get().scopeRegistry.rootScope,
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

        testDispatcher.scheduler.advanceUntilIdle()

        return viewModel
    }
}