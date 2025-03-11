package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.util

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.reply.InitialReplyTextProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection.MemberSelectionBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

class ForwardMessageUseCase(
    courseId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    savedStateHandle: SavedStateHandle,
    coroutineContext: CoroutineContext,
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


}