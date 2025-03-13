package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util

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
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.util.MemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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

    fun createPost(targetConversationId: Long, forwardedSourcePostList: List<ForwardedSourcePostContent>) {
        createPostService.createPost(
            courseId,
            targetConversationId,
            newMessageText.value.text,
            true,
            forwardedSourcePostList = forwardedSourcePostList,
        )
    }

    fun forwardPost(post: IBasePost) {
        viewModelScope.launch(coroutineContext) {
            combine(
                recipients,
                conversations,
                serverConfigurationService.serverUrl,
                accountService.authToken
            ) { recipients, conversations, serverUrl, authToken ->
                Triple(recipients, conversations, serverUrl to authToken)
            }.collect { (recipients, conversations, serverData) ->
                val forwardedSourcePostList = listOf(
                    ForwardedSourcePostContent(
                        sourcePostId = post.serverPostId ?: -1,
                        sourcePostType = when (post) {
                            is IStandalonePost -> PostingType.POST
                            is IAnswerPost -> PostingType.ANSWER
                            else -> PostingType.POST
                        }
                    )
                )

                if (conversations.isNotEmpty()) forwardPostToConversations(
                    conversations,
                    forwardedSourcePostList
                )
                forwardPostToRecipients(recipients, forwardedSourcePostList, serverData)
            }
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
    ) {
        val (serverUrl, authToken) = serverData
        if (recipients.isNotEmpty()) {
            if (recipients.size == 1) {
                conversationService.createOneToOneConversation(
                    courseId,
                    recipients[0].userId,
                    authToken,
                    serverUrl
                ).onSuccess { conversation ->
                    createPost(conversation.id, forwardedSourcePostList = forwardedSourcePostList)
                }.onFailure {
                    forwardingMessageError.value = ForwardingMessageError.DM_CREATION_ERROR
                }
            } else {
                conversationService.createGroupChat(
                    courseId,
                    recipients.map { it.username },
                    authToken,
                    serverUrl
                ).onSuccess { conversation ->
                    createPost(conversation.id, forwardedSourcePostList = forwardedSourcePostList)
                }.onFailure {
                    forwardingMessageError.value = ForwardingMessageError.GROUP_CHAT_CREATION_ERROR
                }
            }
        }
    }
}

enum class ForwardingMessageError {
    NO_ERROR,
    DM_CREATION_ERROR,
    GROUP_CHAT_CREATION_ERROR
}