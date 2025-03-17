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
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.util.MemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
    val onFileSelected: (Uri, Context) -> Unit,
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

    override fun updateInitialReplyText(text: TextFieldValue) {
        newMessageText.value = text
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
        forwardedSourcePostList: List<ForwardedSourcePostContent>
    ) {
        createPostService.createPost(
            courseId,
            targetConversationId,
            newMessageText.value.text,
            true,
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
                forwardedSourcePostList
            )
            // The creation of conversations is more sensitive, so we need to handle errors here.
            val success = forwardPostToRecipients(recipients, forwardedSourcePostList, serverData)
            onComplete(success)
        }
    }

    private fun forwardPostToConversations(
        conversations: List<MemberSelectionItem.Conversation>,
        forwardedSourcePostList: List<ForwardedSourcePostContent>
    ) {
        conversations.forEach {
            createPost(it.id, forwardedSourcePostList)
        }
    }

    private suspend fun forwardPostToRecipients(
        recipients: List<MemberSelectionItem.Recipient>,
        forwardedSourcePostList: List<ForwardedSourcePostContent>,
        serverData: Pair<String, String>
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
                createPost(conversation.id, forwardedSourcePostList = forwardedSourcePostList)
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
                createPost(conversation.id, forwardedSourcePostList = forwardedSourcePostList)
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