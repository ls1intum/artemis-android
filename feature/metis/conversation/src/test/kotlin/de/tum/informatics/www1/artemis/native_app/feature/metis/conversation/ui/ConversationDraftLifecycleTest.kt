package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.StoredReplyDraft
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationDraftLifecycleTest {
    @Test
    fun `last typed draft is persisted when view model is immediately disposed`() = runTest {
        val fixture = Fixture(this)
        try {
            fixture.storage.pauseWrites()
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Last keystroke"))
            fixture.disposeViewModel()
            fixture.storage.resumeWrites()
            runCurrent()
            assertEquals("Last keystroke", fixture.storage.text(null))
        } finally { fixture.close() }
    }

    @Test
    fun `retained composer reloads newer stored draft on returning to conversation`() = runTest {
        val fixture = Fixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Old draft"))
            advanceTimeBy(600)
            runCurrent()
            fixture.viewModel.updateOpenedThread(StandalonePostId.ServerSideId(42))
            runCurrent()
            fixture.storage.updateStoredReplyText("artemis.example", 1, 2, null, "Updated elsewhere")
            fixture.viewModel.updateOpenedThread(null)
            runCurrent()
            assertEquals("Updated elsewhere", fixture.viewModel.newMessageText.value.text)
        } finally { fixture.close() }
    }

    @Test
    fun `visible retained composer adopts a newer externally persisted draft`() = runTest {
        val fixture = Fixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Old draft"))
            advanceTimeBy(600)
            runCurrent()
            fixture.storage.updateStoredReplyText("artemis.example", 1, 2, null, "Updated elsewhere")
            runCurrent()
            assertEquals("Updated elsewhere", fixture.viewModel.newMessageText.value.text)
        } finally { fixture.close() }
    }

    @Test
    fun `unchanged retained draft cannot overwrite a newer stored deletion on leaving`() = runTest {
        val fixture = Fixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Old draft"))
            advanceTimeBy(600)
            runCurrent()
            fixture.storage.updateStoredReplyText("artemis.example", 1, 2, null, "")
            fixture.viewModel.updateOpenedThread(StandalonePostId.ServerSideId(42))
            runCurrent()
            assertEquals("", fixture.storage.text(null))
        } finally { fixture.close() }
    }

    @Test
    fun `answer send snapshots body and parent before coroutine starts`() = runTest {
        val fixture = Fixture(this, StandalonePostId.ServerSideId(42))
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Original reply"))
            val send = fixture.viewModel.createAnswerPost()
            fixture.viewModel.updateOpenedThread(StandalonePostId.ServerSideId(43))
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Other thread draft"))
            runCurrent()
            assertNull(send.await())
            verify(exactly = 1) { fixture.createPost.createAnswerPost(1, 2, 42, "Original reply", any(), any()) }
            assertEquals("Other thread draft", fixture.viewModel.newMessageText.value.text)
        } finally { fixture.close() }
    }

    @Test
    fun `accepted send clears its persisted draft without relying on mounted composer`() = runTest {
        val fixture = Fixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Accepted message"))
            val send = fixture.viewModel.createPost()
            runCurrent()
            assertNull(send.await())
            assertEquals("", fixture.storage.text(null))
            assertEquals("", fixture.viewModel.newMessageText.value.text)
        } finally { fixture.close() }
    }

    @Test
    fun `older accepted answer preserves a newer persisted revision even with identical body`() = runTest {
        for (replacement in listOf("Newer reply", "Original reply")) {
            val fixture = Fixture(this, StandalonePostId.ServerSideId(42))
            try {
                fixture.viewModel.updateInitialReplyText(TextFieldValue("Original reply"))
                val send = fixture.viewModel.createAnswerPost()
                fixture.storage.updateStoredReplyText("artemis.example", 1, 2, 42, "Interim change")
                fixture.storage.updateStoredReplyText("artemis.example", 1, 2, 42, replacement)
                runCurrent()
                assertNull(send.await())
                assertEquals(replacement, fixture.storage.text(42))
                assertEquals(replacement, fixture.viewModel.newMessageText.value.text)
                verify(exactly = 1) { fixture.createPost.createAnswerPost(1, 2, 42, "Original reply", any(), any()) }
            } finally { fixture.close() }
        }
    }

    private class DraftStorage : ReplyTextStorageService {
        private val values = MutableStateFlow<Map<Long?, StoredReplyDraft>>(emptyMap())
        private var writeGate: CompletableDeferred<Unit>? = null
        fun pauseWrites() { writeGate = CompletableDeferred() }
        fun resumeWrites() { writeGate?.complete(Unit) }
        fun text(postId: Long?) = values.value[postId]?.text.orEmpty()
        override suspend fun getStoredReplyText(serverHost: String, courseId: Long, conversationId: Long, postId: Long?) = text(postId)
        override fun observeStoredReplyDraft(serverHost: String, courseId: Long, conversationId: Long, postId: Long?) =
            values.map { it[postId] ?: StoredReplyDraft("") }
        override suspend fun updateStoredReplyText(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, text: String) {
            updateStoredReplyDraft(serverHost, courseId, conversationId, postId, StoredReplyDraft(text, UUID.randomUUID().toString()))
        }
        override suspend fun updateStoredReplyDraft(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, draft: StoredReplyDraft) {
            writeGate?.await()
            values.value = values.value + (postId to draft)
        }
        override suspend fun clearStoredReplyDraftAfterSend(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, sentDraft: StoredReplyDraft): StoredReplyDraft {
            val current = values.value[postId] ?: StoredReplyDraft("")
            if (current == sentDraft) updateStoredReplyText(serverHost, courseId, conversationId, postId, "")
            return values.value[postId] ?: StoredReplyDraft("")
        }
    }

    private class Fixture(private val scope: TestScope, initialPostId: StandalonePostId? = null) {
        val storage = DraftStorage()
        val createPost = mockk<CreatePostService>(relaxed = true)
        val viewModel: ConversationViewModel
        init {
            val dispatcher = StandardTestDispatcher(scope.testScheduler)
            Dispatchers.setMain(dispatcher)
            viewModel = ConversationViewModel(
                courseId = 1,
                conversationId = 2,
                initialPostId = initialPostId,
                websocketProvider = mockk<WebsocketProvider>(relaxed = true) {
                    every { connectionState } returns emptyFlow()
                    every { isConnected } returns flowOf(false)
                },
                metisModificationService = mockk(relaxed = true),
                metisStorageService = mockk<MetisStorageService>(relaxed = true) {
                    every { getLatestKnownPost(any(), any(), any()) } returns flowOf(null)
                },
                savedPostService = mockk(relaxed = true),
                serverConfigurationService = mockk<ServerConfigurationService> {
                    every { serverUrl } returns flowOf("https://artemis.example/")
                    every { host } returns flowOf("artemis.example")
                },
                accountService = mockk<AccountService> {
                    every { authenticationData } returns flowOf(AccountService.AuthenticationData.LoggedIn("token", "user"))
                },
                networkStatusProvider = mockk<NetworkStatusProvider> { every { currentNetworkStatus } returns emptyFlow() },
                conversationService = mockk<ConversationService>(relaxed = true) {
                    coEvery { getConversations(any(), any(), any()) } returns NetworkResponse.Response(listOf(OneToOneChat(id = 2, members = emptyList())))
                },
                replyTextStorageService = storage,
                courseService = mockk(relaxed = true) { every { onArtemisContextChanged } returns emptyFlow() },
                createPostService = createPost,
                faqRepository = mockk(relaxed = true),
                accountDataService = mockk(relaxed = true) { every { onArtemisContextChanged } returns emptyFlow() },
                metisService = mockk(relaxed = true) {
                    coEvery { getPosts(any(), any(), any(), any(), any()) } returns NetworkResponse.Response(emptyList())
                },
                coroutineContext = dispatcher
            )
            scope.runCurrent()
        }
        fun disposeViewModel() {
            viewModel.forwardMessageUseCase.viewModelScope.cancel()
            viewModel.viewModelScope.cancel()
        }
        fun close() { disposeViewModel(); Dispatchers.resetMain() }
    }
}
