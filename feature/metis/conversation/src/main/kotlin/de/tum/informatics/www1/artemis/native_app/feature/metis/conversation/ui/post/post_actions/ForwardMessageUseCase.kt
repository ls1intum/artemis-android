package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.onFailure
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post.util.ForwardedSourcePostContent
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownEditEngine
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.util.MarkdownStyleUtil
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.MemberSelectionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ForwardMessageUseCase(
    courseId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    private val createPostService: CreatePostService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    savedStateHandle: SavedStateHandle,
    private val coroutineContext: CoroutineContext,
    private val onUploadFile: (Uri, Context, (String) -> Unit) -> Unit,
) : MemberSelectionBaseViewModel(
    courseId,
    conversationService,
    accountService,
    serverConfigurationService,
    networkStatusProvider,
    savedStateHandle,
    coroutineContext
), InitialReplyTextProvider {

    val forwardingMessageError = mutableStateOf(ForwardingMessageError.NO_ERROR)

    override val newMessageText: MutableStateFlow<TextFieldValue> =
        MutableStateFlow(TextFieldValue(""))

    private data class ForwardDraftKey(val host: String, val type: PostingType, val postKey: Any)

    val forwardingServerHost: Flow<String> = serverConfigurationService.host
    private var activeDraftKey: ForwardDraftKey? = null
    private val drafts = mutableMapOf<ForwardDraftKey?, TextFieldValue>()
    private val _editorSessionKey = MutableStateFlow<Any?>(null)
    override val editorSessionKey: StateFlow<Any?> = _editorSessionKey

    fun beginForwarding(post: IBasePost) {
        viewModelScope.launch(coroutineContext, start = CoroutineStart.UNDISPATCHED) {
            val key = ForwardDraftKey(
                host = forwardingServerHost.first(),
                type = if (post is IAnswerPost) PostingType.ANSWER else PostingType.POST,
                postKey = post.serverPostId ?: post.clientPostId ?: post.key
            )
            activeDraftKey = key
            val text = drafts[key]?.copy(composition = null) ?: TextFieldValue()
            drafts[key] = text
            newMessageText.value = text
            _editorSessionKey.value = key
        }
    }

    override fun updateInitialReplyText(text: TextFieldValue) {
        drafts[activeDraftKey] = text
        newMessageText.value = text
    }

    override fun captureAttachmentTarget(): (String) -> Unit {
        val target = activeDraftKey
        return { markdown ->
            val latest = drafts[target] ?: TextFieldValue()
            val updated = MarkdownEditEngine.insertMarkup(latest, markdown)
            drafts[target] = updated
            if (activeDraftKey == target) newMessageText.value = updated
        }
    }

    fun onFileSelected(
        uri: Uri,
        context: Context,
        onAppendMarkdown: (String) -> Unit = captureAttachmentTarget()
    ) {
        onUploadFile(uri, context, onAppendMarkdown)
    }

    fun resetErrorMessage() {
        forwardingMessageError.value = ForwardingMessageError.NO_ERROR
    }

    /**
     * Passes the information to the worker, which creates or schedules the post.
     * @param targetConversationId the id of the conversation to create the post in
     * @param forwardedSourcePostList a list of posts that are forwarded in the new post
     */
    fun createPost(
        targetConversationId: Long,
        forwardedSourcePostList: List<ForwardedSourcePostContent>,
        comment: String = MarkdownStyleUtil.prepareForSend(newMessageText.value.text)
    ) {
        createPostService.createPost(
            courseId,
            targetConversationId,
            comment,
            forwardedSourcePostList = forwardedSourcePostList,
        )
    }

    /**
     * Forwards a post to the selected recipients and conversations.
     * If the post is forwarded to a single recipient, a direct message is created.
     * If the post is forwarded to multiple recipients, a group chat is created.
     * @param post the post to forward
     * @param onComplete callback that is called when the forwarding is completed
     * Note: This function was inspired by the Artemis web app to ensure equal functionality
     * https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/shared/metis/posting-reactions-bar/posting-reactions-bar.component.ts
     */
    fun forwardPost(post: IBasePost, onComplete: (Boolean) -> Unit) {
        val comment = MarkdownStyleUtil.prepareForSend(newMessageText.value.text)
        viewModelScope.launch(coroutineContext) {
            val recipients = recipients.first()
            val conversations = conversations.first()
            val serverData =
                (serverConfigurationService.serverUrl.first() to accountService.authToken.first())

            val forwardedSourcePostList = listOf(
                ForwardedSourcePostContent(
                    sourcePostId = post.serverPostId ?: return@launch,
                    sourcePostType = when (post) {
                        is IStandalonePost -> PostingType.POST
                        is IAnswerPost -> PostingType.ANSWER
                        else -> PostingType.POST
                    }
                )
            )

            // We don't expect errors to happen when forwarding to conversations, since the sending
            // can be scheduled in the background.
            if (conversations.isNotEmpty()) forwardPostToConversations(
                conversations,
                forwardedSourcePostList,
                comment
            )
            // The creation of conversations is more sensitive, so we need to handle errors here.
            val success = forwardPostToRecipients(recipients, forwardedSourcePostList, serverData, comment)
            onComplete(success)
        }
    }

    private fun forwardPostToConversations(
        conversations: List<MemberSelectionItem.Conversation>,
        forwardedSourcePostList: List<ForwardedSourcePostContent>,
        comment: String
    ) {
        conversations.forEach {
            createPost(it.id, forwardedSourcePostList, comment)
        }
    }

    private suspend fun forwardPostToRecipients(
        recipients: List<MemberSelectionItem.Recipient>,
        forwardedSourcePostList: List<ForwardedSourcePostContent>,
        serverData: Pair<String, String>,
        comment: String
    ): Boolean {
        val (serverUrl, authToken) = serverData
        if (recipients.isEmpty()) return true

        if (recipients.size == 1) {
            conversationService.createOneToOneConversation(
                courseId,
                recipients[0].userId,
                authToken,
                serverUrl
            ).onSuccess { conversation ->
                createPost(conversation.id, forwardedSourcePostList = forwardedSourcePostList, comment = comment)
                return true
            }.onFailure {
                forwardingMessageError.value = ForwardingMessageError.DM_CREATION_ERROR
                return false
            }
        } else {
            conversationService.createGroupChat(
                courseId,
                recipients.map { it.username },
                authToken,
                serverUrl
            ).onSuccess { conversation ->
                createPost(conversation.id, forwardedSourcePostList = forwardedSourcePostList, comment = comment)
                return true
            }.onFailure {
                forwardingMessageError.value = ForwardingMessageError.GROUP_CHAT_CREATION_ERROR
                return false
            }
        }
        return true
    }
}

enum class ForwardingMessageError {
    NO_ERROR,
    DM_CREATION_ERROR,
    GROUP_CHAT_CREATION_ERROR
}
