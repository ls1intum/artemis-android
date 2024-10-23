package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.AccountDataServiceStub
import de.tum.informatics.www1.artemis.native_app.core.data.CourseServiceFake
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountServiceStub
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProviderStub
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.test.TestWebsocketProvider
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.testWebsocketModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.TestServerConfigurationProvider
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.createCourse
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.runner.RunWith
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationThreadScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.ConversationViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ConversationServiceStub
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import org.junit.experimental.categories.Category
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Category(EndToEndTest::class)
class ConversationAnswerMessagesUITest : KoinTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, testLoginModule, testWebsocketModule)
    }

    private val mockMetisService = mockk<MetisService>()
    private val mockMetisModificationService = mockk<MetisModificationService>()
    private val mockMetisStorageService = mockk<MetisStorageService>()
    private val mockCreatePostService = mockk<CreatePostService>()
    private val mockReplyTextStorageService = mockk<ReplyTextStorageService>()
    private val mockConversation = mockk<Conversation>()

    private lateinit var course: Course

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun setUp() {
        runBlocking {
            withTimeout(DefaultTestTimeoutMillis) {
                performTestLogin()
                course = createCourse(getAdminAccessToken())
            }
        }
        coEvery { mockMetisService.getPost(any(), any(), any(), any()) } returns mockPostResponse()
    }

    @Test
    fun `resolve post in UI`() {
        setupViewModelAndUi()

        composeTestRule.onNodeWithText("answer post test message").performClick()
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.post_resolves)).performClick()

        coVerify { mockMetisModificationService.updateAnswerPost(any(), any(), any(), any()) }
    }

    private fun mockPostResponse(): NetworkResponse<StandalonePost> {
        val post = PostPojo(
            clientPostId = "200",
            serverPostId = 200L,
            title = "Title",
            content = "Message",
            authorName = "Name",
            authorRole = UserRole.TUTOR,
            authorId = 132L,
            creationDate = Clock.System.now(),
            updatedDate = null,
            resolved = false,
            courseWideContext = null,
            tags = emptyList(),
            answers = listOf(),
            reactions = emptyList()
        )

        val standalonePost = StandalonePost(
            post = post,
            conversation = mockConversation
        )

        return NetworkResponse.Success(standalonePost)
    }

    private fun setupViewModelAndUi(): ConversationViewModel {

        val viewModel = ConversationViewModel(
            courseId = course.id!!,
            conversationId = mockConversation.id,
            initialPostId = StandalonePostId.ClientSideId("ID"),
            websocketProvider = TestWebsocketProvider(),
            metisModificationService = mockMetisModificationService,
            metisStorageService = mockMetisStorageService,
            serverConfigurationService = TestServerConfigurationProvider(),
            accountService = AccountServiceStub(),
            networkStatusProvider = NetworkStatusProviderStub(),
            conversationService = ConversationServiceStub(emptyList()),
            replyTextStorageService = mockReplyTextStorageService,
            courseService = CourseServiceFake(course),
            createPostService = mockCreatePostService,
            accountDataService = AccountDataServiceStub(),
            metisService = mockMetisService,
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            ConversationThreadScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                onNavigateUp = {}
            )
        }

        return viewModel
    }
}