package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.members

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.SettingsBaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.shareIn

internal class ConversationMembersViewModel(
    courseId: Long,
    conversationId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    serverDataService: ServerDataService,
    private val savedStateHandle: SavedStateHandle
) : SettingsBaseViewModel(
    courseId,
    conversationId,
    conversationService,
    accountService,
    serverConfigurationService,
    networkStatusProvider,
    serverDataService
) {
    companion object {
        private const val KEY_QUERY = "query"
    }

    val conversation: StateFlow<DataState<Conversation>> = loadedConversation

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val membersPagingData: Flow<PagingData<ConversationUser>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        query
    ) { serverUrl, authToken, query ->
        Pager(
            config = PagingConfig(10)
        ) {
            MembersDataSource(
                courseId,
                conversationId,
                serverUrl,
                authToken,
                query,
                conversationService
            )
        }
            .flow
    }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    fun updateQuery(newQuery: String) {
        savedStateHandle[KEY_QUERY] = newQuery
    }
}