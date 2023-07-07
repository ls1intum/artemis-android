package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.MemberSelectionBaseViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class CreatePersonalConversationViewModel(
    courseId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    savedStateHandle: SavedStateHandle,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : MemberSelectionBaseViewModel(
    courseId,
    conversationService,
    accountService,
    serverConfigurationService,
    networkStatusProvider,
    savedStateHandle,
    coroutineContext
) {

    val canCreateConversation: StateFlow<Boolean> = recipients
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    fun createConversation(): Deferred<Conversation?> {
        return viewModelScope.async(coroutineContext) {
            val authToken = accountService.authToken.first()
            val serverUrl = serverConfigurationService.serverUrl.first()

            val currentRecipients = recipients.value

            val recipientsAsUsernames = currentRecipients.map { it.username }

            val result = if (currentRecipients.size > 1) {
                conversationService.createGroupChat(
                    courseId,
                    recipientsAsUsernames,
                    authToken,
                    serverUrl
                ).orNull()
            } else if (currentRecipients.isNotEmpty()) {
                conversationService.createOneToOneConversation(
                    courseId,
                    recipientsAsUsernames.first(),
                    authToken,
                    serverUrl
                ).orNull()
            } else null

            if (result != null) {
                reset()
            }

            result
        }
    }
}
