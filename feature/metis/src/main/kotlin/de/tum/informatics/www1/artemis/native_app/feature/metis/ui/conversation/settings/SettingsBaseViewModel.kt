package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.ServerDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.getConversation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

abstract class SettingsBaseViewModel(
    protected val courseId: Long,
    protected val conversationId: Long,
    protected val conversationService: ConversationService,
    protected val accountService: AccountService,
    protected val serverConfigurationService: ServerConfigurationService,
    protected val networkStatusProvider: NetworkStatusProvider,
    serverDataService: ServerDataService
) : ViewModel() {

    protected val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    protected val loadedConversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        onRequestReload.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService
                .getConversation(courseId, conversationId, authToken, serverUrl)
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    val clientUsername: StateFlow<DataState<String>> = flatMapLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        onRequestReload.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            serverDataService.getAccountData(serverUrl, authToken)
        }
            .map { accountDataState -> accountDataState.bind { it.username.orEmpty() } }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    fun kickMember(username: String): Deferred<Boolean> {
        return performActionOnUser(username, ConversationService::kickMember)
    }

    fun grantModerationRights(conversationUser: ConversationUser): Deferred<Boolean> {
        return performActionOnUser(conversationUser.username.orEmpty(), ConversationService::grantModerationRights)
    }

    fun revokeModerationRights(conversationUser: ConversationUser): Deferred<Boolean> {
        return performActionOnUser(conversationUser.username.orEmpty(), ConversationService::revokeModerationRights)
    }

    private fun performActionOnUser(
        username: String,
        action: suspend ConversationService.(Long, Conversation, String, String, String) -> NetworkResponse<Boolean>
    ): Deferred<Boolean> {
        return viewModelScope.async {
            val conversation = loadedConversation.value.orNull() ?: return@async false

            conversationService.action(
                courseId,
                conversation,
                username,
                accountService.authToken.first(),
                serverConfigurationService.serverUrl.first()
            )
                .or(false)
                .also { successful ->
                    if (successful) {
                        onRequestReload.tryEmit(Unit)
                    }
                }
        }
    }

    open fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}