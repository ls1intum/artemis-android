package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.TextRange
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.MemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import io.mockk.verify
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.websocket.WebsocketProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.model.FileUploadResponse
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisModificationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.MetisStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.ReplyTextStorageService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.storage.StoredReplyDraft
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.StandalonePostId
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.StandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.AnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.AnswerPostPojo
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import java.util.UUID
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class ConversationAttachmentRoutingTest {

    @Test
    fun `successful post edit updates local storage from server response without websocket`() = runTest {
        val fixture = UploadFixture(this)
        try {
            val result = fixture.viewModel.editPost(StandalonePost(id = 61, content = "Before"), "Requested edit")
            runCurrent()
            assertNull(result.await())
            assertEquals("Server normalized edit", fixture.updatedPosts.single().content)
            assertEquals(61L, fixture.updatedPosts.single().id)
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `successful answer edit requests fresh conversation data without websocket`() = runTest {
        val fixture = UploadFixture(this)
        try {
            val refreshesBeforeEdit = fixture.postRefreshCount
            val answer = mockk<AnswerPostPojo>(relaxed = true)
            val result = fixture.viewModel.editAnswerPost(StandalonePost(id = 61), answer, "Edited answer")
            runCurrent()
            assertNull(result.await())
            assertTrue(fixture.postRefreshCount > refreshesBeforeEdit)
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `server switch preserves originating upload credentials URL and draft storage`() = runTest {
        val fixture = UploadFixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Server A", TextRange(8)))
            fixture.viewModel.onFileSelected(
                fixture.fileUri, fixture.context, fixture.viewModel.captureAttachmentTarget()
            )
            fixture.switchServer("https://other.example/", "token-b")
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Server B"))
            fixture.awaitUploadStarted()
            fixture.completeUpload()
            advanceTimeBy(600)
            runCurrent()

            assertEquals("https://artemis.example/" to "token-a", fixture.uploadRequest)
            assertEquals("Server B", fixture.viewModel.newMessageText.value.text)
            assertEquals("Server B", fixture.storedDraft(null, "other.example"))
            assertEquals(
                "Server A[report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.storedDraft(null)
            )
            fixture.switchServer("https://artemis.example/", "token-a")
            assertEquals(
                "Server A[report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.viewModel.newMessageText.value.text
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `attachment rechecks latest typing after delayed draft storage read`() = runTest {
        val fixture = UploadFixture(this, delayDraftReads = true, initialDraft = "Stored draft")
        try {
            fixture.viewModel.captureAttachmentTarget()(" [report.pdf](https://artemis.example/api/files/report.pdf)")
            runCurrent()
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Typed while loading", TextRange(19)))
            fixture.releaseDraftReads()

            assertEquals(
                "Typed while loading [report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.viewModel.newMessageText.value.text
            )
            assertEquals(
                "Typed while loading [report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.storedDraft(null)
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `conversation attachment stays with original draft after opening a thread`() = runTest {
        assertUploadKeepsOriginalDraft(initialPostId = null)
    }

    @Test
    fun `thread attachment stays with original draft after opening another thread`() = runTest {
        assertUploadKeepsOriginalDraft(initialPostId = StandalonePostId.ServerSideId(42))
    }

    @Test
    fun `autosave keeps draft identity when switching threads before debounce finishes`() = runTest {
        val fixture = UploadFixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Conversation draft"))
            fixture.viewModel.updateOpenedThread(StandalonePostId.ServerSideId(42))
            runCurrent()
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Thread draft"))
            advanceTimeBy(600)
            runCurrent()

            assertEquals("Conversation draft", fixture.storedDraft(null))
            assertEquals("Thread draft", fixture.storedDraft(42))
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `pending autosave cannot overwrite a completed attachment`() = runTest {
        val fixture = UploadFixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Draft", TextRange(5)))
            fixture.viewModel.onFileSelected(
                fixture.fileUri,
                fixture.context,
                fixture.viewModel.captureAttachmentTarget()
            )
            fixture.awaitUploadStarted()
            fixture.completeUpload()
            advanceTimeBy(600)
            runCurrent()

            assertEquals(
                "Draft[report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.storedDraft(null)
            )
            assertEquals(fixture.storedDraft(null), fixture.viewModel.newMessageText.value.text)
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `forward attachment updates latest forward draft without changing normal draft`() = runTest {
        val fixture = UploadFixture(this)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Normal draft"))
            fixture.viewModel.forwardMessageUseCase.updateInitialReplyText(TextFieldValue("Forward draft"))

            fixture.viewModel.forwardMessageUseCase.onFileSelected(fixture.fileUri, fixture.context)
            fixture.awaitUploadStarted()

            // The user keeps typing while the server handles the upload.
            fixture.viewModel.forwardMessageUseCase.updateInitialReplyText(
                TextFieldValue("Forward draft continued", selection = TextRange(23))
            )
            fixture.completeUpload()

            assertEquals("Normal draft", fixture.viewModel.newMessageText.value.text)
            assertEquals(
                "Forward draft continued[report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.viewModel.forwardMessageUseCase.newMessageText.value.text
            )
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `forward send snapshots comment before scheduled fan-out`() = runTest {
        val fixture = UploadFixture(this)
        try {
            val useCase = fixture.viewModel.forwardMessageUseCase
            useCase.addMemberItem(MemberSelectionItem.Conversation("general", 2, Icons.Default.Numbers))
            runCurrent()
            useCase.updateInitialReplyText(TextFieldValue("Original comment"))
            useCase.forwardPost(StandalonePost(id = 10, content = "Source")) {}
            useCase.updateInitialReplyText(TextFieldValue("Another draft"))
            runCurrent()
            verify(exactly = 1) { fixture.createPostService.createPost(1, 2, "Original comment", any(), any()) }
        } finally {
            fixture.close()
        }
    }

    @Test
    fun `forward attachment keeps launch-time source after switching and reopening sheets`() = runTest {
        val fixture = UploadFixture(this)
        try {
            val useCase = fixture.viewModel.forwardMessageUseCase
            val firstSource = StandalonePost(id = 10, content = "First source")
            val secondSource = StandalonePost(id = 11, content = "Second source")
            useCase.beginForwarding(firstSource)
            runCurrent()
            useCase.updateInitialReplyText(TextFieldValue("First comment", TextRange(13)))
            val firstSession = useCase.editorSessionKey.value
            val preparedTarget = useCase.captureAttachmentTarget()

            // The sheet changes while the system picker is still open.
            useCase.beginForwarding(secondSource)
            runCurrent()
            assertEquals("", useCase.newMessageText.value.text)
            assertNotEquals(firstSession, useCase.editorSessionKey.value)
            useCase.updateInitialReplyText(TextFieldValue("Second comment"))
            useCase.onFileSelected(fixture.fileUri, fixture.context, preparedTarget)
            fixture.awaitUploadStarted()
            fixture.completeUpload()

            assertEquals("Second comment", useCase.newMessageText.value.text)
            useCase.beginForwarding(firstSource)
            runCurrent()
            assertEquals(
                "First comment[report.pdf](https://artemis.example/api/files/report.pdf)",
                useCase.newMessageText.value.text
            )
            useCase.beginForwarding(secondSource)
            runCurrent()
            assertEquals("Second comment", useCase.newMessageText.value.text)
        } finally {
            fixture.close()
        }
    }

    private suspend fun TestScope.assertUploadKeepsOriginalDraft(initialPostId: StandalonePostId?) {
        val fixture = UploadFixture(this, initialPostId)
        try {
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Original draft", TextRange(14)))
            val appendToOriginal = fixture.viewModel.captureAttachmentTarget()
            fixture.viewModel.onFileSelected(fixture.fileUri, fixture.context, appendToOriginal)
            fixture.awaitUploadStarted()

            fixture.viewModel.updateInitialReplyText(TextFieldValue("Original draft continued", TextRange(24)))
            fixture.viewModel.updateOpenedThread(StandalonePostId.ServerSideId(43))
            runCurrent()
            fixture.viewModel.updateInitialReplyText(TextFieldValue("Other thread draft"))
            fixture.completeUpload()

            assertEquals("Other thread draft", fixture.viewModel.newMessageText.value.text)
            val storedPostId = (initialPostId as? StandalonePostId.ServerSideId)?.serverSidePostId
            assertEquals(
                "Original draft continued[report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.storedDraft(storedPostId)
            )

            fixture.viewModel.updateOpenedThread(initialPostId)
            runCurrent()
            assertEquals(
                "Original draft continued[report.pdf](https://artemis.example/api/files/report.pdf)",
                fixture.viewModel.newMessageText.value.text
            )
        } finally {
            fixture.close()
        }
    }

    private class UploadFixture(
        private val scope: TestScope,
        initialPostId: StandalonePostId? = null,
        delayDraftReads: Boolean = false,
        initialDraft: String = ""
    ) {
        val context: Context = RuntimeEnvironment.getApplication()
        private val file = File(context.cacheDir, "report.pdf").apply { writeText("Attachment contents") }
        val fileUri: Uri = Uri.fromFile(file)

        private val uploadStarted = CompletableDeferred<Unit>()
        private val response = CompletableDeferred<NetworkResponse<FileUploadResponse>>()
        var uploadRequest: Pair<String, String>? = null
            private set
        private val modificationService = mockk<MetisModificationService> {
            coEvery { updateStandalonePost(any(), any(), any(), any()) } coAnswers {
                NetworkResponse.Response(arg<StandalonePost>(1).copy(content = "Server normalized edit"))
            }
            coEvery { updateAnswerPost(any(), any(), any(), any()) } coAnswers {
                NetworkResponse.Response(arg<AnswerPost>(1))
            }
            coEvery { uploadFileOrImage(any(), any(), any(), any(), any()) } coAnswers {
                uploadRequest = arg<String>(3) to arg<String>(4)
                uploadStarted.complete(Unit)
                response.await()
            }
        }
        private val selectedServerUrl = MutableStateFlow("https://artemis.example/")
        private val authentication = MutableStateFlow<AccountService.AuthenticationData>(
            AccountService.AuthenticationData.LoggedIn("token-a", "user")
        )
        private val serverConfiguration = mockk<ServerConfigurationService> {
            every { serverUrl } returns selectedServerUrl
            every { host } returns selectedServerUrl.map { URI(it).host }
        }
        private val accountService = mockk<AccountService> {
            every { authenticationData } returns authentication
        }
        private val network = mockk<NetworkStatusProvider> {
            every { currentNetworkStatus } returns emptyFlow()
        }
        private val websocket = mockk<WebsocketProvider>(relaxed = true) {
            every { connectionState } returns emptyFlow()
            every { isConnected } returns flowOf(false)
        }
        val updatedPosts = mutableListOf<StandalonePost>()
        private val storage = mockk<MetisStorageService>(relaxed = true) {
            every { getLatestKnownPost(any(), any(), any()) } returns flowOf(null)
            coEvery { updatePost(any(), any(), any()) } coAnswers { updatedPosts += arg<StandalonePost>(2) }
        }
        private val storedDrafts = mutableMapOf<Pair<String, Long?>, String>(
            ("artemis.example" to (initialPostId as? StandalonePostId.ServerSideId)?.serverSidePostId) to initialDraft
        )
        private val draftReadGate = CompletableDeferred<Unit>().apply {
            if (!delayDraftReads) complete(Unit)
        }
        private val storedDraftRevisions = mutableMapOf<Pair<String, Long?>, String?>()
        private val draftChanges = MutableStateFlow(0)
        private val replyStorage = object : ReplyTextStorageService {
            override fun observeStoredReplyDraft(serverHost: String, courseId: Long, conversationId: Long, postId: Long?) = flow {
                val initial = getStoredReplyText(serverHost, courseId, conversationId, postId)
                emit(StoredReplyDraft(initial, storedDraftRevisions[serverHost to postId]))
                emitAll(draftChanges.map {
                    StoredReplyDraft(storedDrafts[serverHost to postId].orEmpty(), storedDraftRevisions[serverHost to postId])
                })
            }

            override suspend fun updateStoredReplyDraft(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, draft: StoredReplyDraft) {
                storedDrafts[serverHost to postId] = draft.text
                storedDraftRevisions[serverHost to postId] = draft.revision
                draftChanges.value++
            }

            override suspend fun clearStoredReplyDraftAfterSend(serverHost: String, courseId: Long, conversationId: Long, postId: Long?, sentDraft: StoredReplyDraft): StoredReplyDraft {
                val current = StoredReplyDraft(storedDrafts[serverHost to postId].orEmpty(), storedDraftRevisions[serverHost to postId])
                if (current == sentDraft) updateStoredReplyText(serverHost, courseId, conversationId, postId, "")
                return StoredReplyDraft(storedDrafts[serverHost to postId].orEmpty(), storedDraftRevisions[serverHost to postId])
            }

            override suspend fun getStoredReplyText(
                serverHost: String,
                courseId: Long,
                conversationId: Long,
                postId: Long?
            ): String {
                val text = storedDrafts[serverHost to postId].orEmpty()
                draftReadGate.await()
                return text
            }

            override suspend fun updateStoredReplyText(
                serverHost: String,
                courseId: Long,
                conversationId: Long,
                postId: Long?,
                text: String
            ) {
                updateStoredReplyDraft(serverHost, courseId, conversationId, postId, StoredReplyDraft(text, UUID.randomUUID().toString()))
            }
        }
        private val conversationService = mockk<ConversationService>(relaxed = true) {
            coEvery { getConversations(any(), any(), any()) } returns
                NetworkResponse.Response(listOf(OneToOneChat(id = 2, members = emptyList())))
        }
        var postRefreshCount = 0
            private set
        private val metisService = mockk<MetisService>(relaxed = true) {
            coEvery { getPosts(any(), any(), any(), any(), any()) } coAnswers {
                postRefreshCount++
                NetworkResponse.Response(emptyList())
            }
        }

        val createPostService = mockk<CreatePostService>(relaxed = true)
        val viewModel: ConversationViewModel

        init {
            val dispatcher = StandardTestDispatcher(scope.testScheduler)
            Dispatchers.setMain(dispatcher)
            viewModel = ConversationViewModel(
                courseId = 1,
                conversationId = 2,
                initialPostId = initialPostId,
                websocketProvider = websocket,
                metisModificationService = modificationService,
                metisStorageService = storage,
                savedPostService = mockk(relaxed = true),
                serverConfigurationService = serverConfiguration,
                accountService = accountService,
                networkStatusProvider = network,
                conversationService = conversationService,
                replyTextStorageService = replyStorage,
                courseService = mockk(relaxed = true) {
                    every { onArtemisContextChanged } returns emptyFlow()
                },
                createPostService = createPostService,
                faqRepository = mockk(relaxed = true),
                accountDataService = mockk(relaxed = true) {
                    every { onArtemisContextChanged } returns emptyFlow()
                },
                metisService = metisService,
                coroutineContext = dispatcher
            )
            scope.runCurrent()
        }

        suspend fun awaitUploadStarted() {
            // File reads use Dispatchers.IO; wait on that boundary with a real clock.
            withContext(Dispatchers.IO) {
                withTimeout(5_000) { uploadStarted.await() }
            }
        }

        fun completeUpload() {
            response.complete(NetworkResponse.Response(FileUploadResponse("/api/files/report.pdf")))
            scope.runCurrent()
        }

        fun storedDraft(postId: Long?, host: String = "artemis.example"): String? = storedDrafts[host to postId]

        fun switchServer(serverUrl: String, authToken: String) {
            selectedServerUrl.value = serverUrl
            authentication.value = AccountService.AuthenticationData.LoggedIn(authToken, "user")
            scope.runCurrent()
        }

        fun releaseDraftReads() {
            draftReadGate.complete(Unit)
            scope.runCurrent()
        }

        fun close() {
            viewModel.forwardMessageUseCase.viewModelScope.cancel()
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
            file.delete()
        }
    }
}
