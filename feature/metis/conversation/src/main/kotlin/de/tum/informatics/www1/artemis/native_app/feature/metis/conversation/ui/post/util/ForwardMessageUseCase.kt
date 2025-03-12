package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.CreatePostService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service.network.MetisService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.PostingType
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ForwardMessageUseCase(
    courseId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    private val metisService: MetisService,
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

    override val newMessageText: MutableStateFlow<TextFieldValue> =
        MutableStateFlow(TextFieldValue(""))

    override fun updateInitialReplyText(text: TextFieldValue) {
        newMessageText.value = text
    }

    fun createPost(targetConversationId: Long, forwardedSourcePostList: List<ForwardedSourcePostContent>): Deferred<MetisModificationFailure?> {
        createPostService.createPost(
            courseId,
            targetConversationId,
            newMessageText.value.text,
            true,
            forwardedSourcePostList = forwardedSourcePostList,
        )

        return CompletableDeferred(value = null)
    }

    fun forwardPost(post: IBasePost) {
        viewModelScope.launch(coroutineContext) {
            combine(recipients, conversations) { recipients, conversations ->
                if (recipients.isNotEmpty()) {
                    if (recipients.size == 1) {
                        // create DM
                    } else {
                        // create group
                    }
                }

                createPost(conversations[0].id, forwardedSourcePostList = listOf(
                    ForwardedSourcePostContent(
                        sourcePostId = post.serverPostId ?: -1,
                        sourcePostType = when (post) {
                            is IStandalonePost -> PostingType.POST
                            is IAnswerPost -> PostingType.ANSWER
                            else -> PostingType.POST
                        }
                    )
                ))

            }.collect { }
        }
    }
}