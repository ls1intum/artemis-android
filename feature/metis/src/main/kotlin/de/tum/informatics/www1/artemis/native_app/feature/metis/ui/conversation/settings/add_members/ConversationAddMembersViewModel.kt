package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.add_members

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection.MemberSelectionBaseViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ConversationAddMembersViewModel(
    courseId: Long,
    private val conversationId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    savedStateHandle: SavedStateHandle
) : MemberSelectionBaseViewModel(
    courseId,
    conversationService,
    accountService,
    serverConfigurationService,
    networkStatusProvider,
    savedStateHandle
) {

    val canAdd: StateFlow<Boolean> = recipients
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun addMembers(): Deferred<Boolean> {
        return viewModelScope.async {
            val conversation = loadConversation() ?: return@async false

            conversationService.registerMembers(
                courseId,
                conversation,
                recipients.first().map { it.username },
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
                .or(false)
        }
    }

    private suspend fun loadConversation(): Conversation? {
        return conversationService
            .getConversation(
                courseId,
                conversationId,
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
            .orNull()
    }
}