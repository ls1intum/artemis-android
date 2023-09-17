package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.members

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.SettingsBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

internal class ConversationMembersViewModel(
    initialCourseId: Long,
    initialConversationId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    accountDataService: AccountDataService,
    private val savedStateHandle: SavedStateHandle,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : SettingsBaseViewModel(
    initialCourseId,
    initialConversationId,
    conversationService,
    accountService,
    serverConfigurationService,
    networkStatusProvider,
    accountDataService,
    coroutineContext
) {
    companion object {
        private const val KEY_QUERY = "query"
    }

    val conversation: StateFlow<DataState<Conversation>> = loadedConversation

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val membersPagingData: Flow<PagingData<ConversationUser>> = flatMapLatest(
        onRequestReload.onStart { emit(Unit) },
        conversationSettings,
        serverConfigurationService.serverUrl,
        accountService.authToken,
        query.debounce(200.milliseconds)
    ) { _, conversationSettings, serverUrl, authToken, query ->
        Pager(
            config = PagingConfig(10)
        ) {
            MembersDataSource(
                courseId = conversationSettings.courseId,
                conversationId = conversationSettings.conversationId,
                serverUrl = serverUrl,
                authToken = authToken,
                query = query,
                conversationService = conversationService
            )
        }
            .flow
            .cachedIn(viewModelScope + coroutineContext)
    }
        .shareIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, replay = 1)

    fun updateQuery(newQuery: String) {
        savedStateHandle[KEY_QUERY] = newQuery
    }
}
