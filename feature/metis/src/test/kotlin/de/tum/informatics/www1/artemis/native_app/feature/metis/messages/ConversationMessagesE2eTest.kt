package de.tum.informatics.www1.artemis.native_app.feature.metis.messages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.printToString
import androidx.paging.compose.collectAsLazyPagingItems
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationBaseTest
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.DisplayPriority
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.TEST_TAG_METIS_POST_LIST
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.testTagForPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.ProvideLocalVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.visible_metis_context_reporter.VisibleMetisContextManager
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationMessagesE2eTest : ConversationBaseTest() {

    private lateinit var conversation: Conversation
    private lateinit var metisContext: MetisContext

    private val metisModificationService: MetisModificationService get() = get()

    @Before
    override fun setup() {
        super.setup()

        runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                conversation = conversationService.createOneToOneConversation(
                    courseId = course.id!!,
                    partner = user2Username,
                    authToken = accessToken,
                    serverUrl = testServerUrl
                ).orThrow("Could not create one to one conversation")

                metisContext = MetisContext.Conversation(course.id!!, conversation.id)
            }
        }
    }

    @Test
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

        posts.forEach { post ->
            composeTestRule.waitUntilExactlyOneExists(hasTestTag(testTagForPost(post.serverPostId)))

            composeTestRule
                .onNodeWithTag(testTagForPost(post.serverPostId))
                .performScrollTo()
                .assertExists("Post $post does not exist")
        }
    }

    private fun setupUiAndViewModel(dispatcher: TestDispatcher = UnconfinedTestDispatcher()): MetisListViewModel {
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
            coroutineContext = dispatcher
        )

        composeTestRule.setContent {
            ProvideLocalVisibleMetisContextManager(visibleMetisContextManager = EmptyVisibleMetisContextManager) {
                MetisChatList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    listContentPadding = PaddingValues(),
                    onClickViewPost = {},
                )
            }
        }

        dispatcher.scheduler.advanceTimeBy(20000)
        dispatcher.scheduler.advanceUntilIdle()

        composeTestRule.waitUntilExactlyOneExists(
            hasTestTag(TEST_TAG_METIS_POST_LIST),
            DefaultTimeoutMillis
        )

        return viewModel
    }

    private fun createPost(content: String): StandalonePost {
        return StandalonePost(
            id = null,
            title = null,
            tags = null,
            content = content,
            conversation = conversation,
            creationDate = Clock.System.now(),
            displayPriority = DisplayPriority.NONE
        )
    }
}