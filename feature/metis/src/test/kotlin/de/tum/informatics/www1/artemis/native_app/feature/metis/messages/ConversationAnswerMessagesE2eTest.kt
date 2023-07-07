package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.TEST_TAG_METIS_POST_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.testTagForPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.reply.TEST_TAG_REPLY_SEND_BUTTON
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.MetisThreadUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.MetisThreadViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.TEST_TAG_THREAD_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.testTagForAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ProvideLocalVisibleMetisContextManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationAnswerMessagesE2eTest : ConversationMessagesBaseTest() {

    @Test
    fun `shows existing message with answer posts`() {
        val answerPostContents = (0 until 3).map {
            "answer post content $it"
        }

        val (post, answerPosts) = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                val post = metisModificationService.createPost(
                    context = metisContext,
                    post = createPost("test message"),
                    serverUrl = testServerUrl,
                    authToken = accessToken
                ).orThrow("Could not create message")


                val answerPosts = answerPostContents.map { replyText ->
                    metisModificationService.createAnswerPost(
                        context = metisContext,
                        post = AnswerPost(
                            creationDate = Clock.System.now(),
                            content = replyText,
                            post = post
                        ),
                        serverUrl = testServerUrl,
                        authToken = accessToken
                    ).orThrow("Could not create answer message with text $post")
                }

                post to answerPosts
            }
        }

        setupUiAndViewModel(post.id!!)

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_THREAD_LIST),
            DefaultTimeoutMillis
        )

        answerPosts.forEach { answerPost ->
            composeTestRule.waitUntilExactlyOneExists(hasTestTag(testTagForAnswerPost(answerPost.serverPostId)))

            composeTestRule
                .onNodeWithTag(testTagForAnswerPost(answerPost.serverPostId))
                .performScrollTo()
                .assertExists("Answer post $answerPost does not exist")
        }
    }

    @Test(timeout = 10000)
    fun `can send new message`() {
        val post = postDefaultMessage()

        setupUiAndViewModel(post.id!!)

        canSendTestImpl(
            "test message",
            TEST_TAG_THREAD_LIST
        )
    }

    private fun setupUiAndViewModel(postId: Long): MetisThreadViewModel {
        val viewModel = MetisThreadViewModel(
            initialPostId = StandalonePostId.ServerSideId(postId),
            subscribeToLiveUpdateService = true,
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
            ProvideLocalVisibleMetisContextManager(visibleMetisContextManager = EmptyVisibleMetisContextManager) {
                MetisThreadUi(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Wait until post and answer posts are loaded
        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_THREAD_LIST),
            DefaultTimeoutMillis
        )

        return viewModel
    }

}