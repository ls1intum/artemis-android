package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.user_conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.UserIdentifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart

internal class NavigateToUserConversationViewModel(
    private val courseId: Long,
    private val userIdentifier: UserIdentifier,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    conversationService: ConversationService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider
) : ViewModel() {

    private val requestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val accountData: StateFlow<DataState<Account>> = accountDataService
        .performAutoReloadingNetworkCall(
            networkStatusProvider = networkStatusProvider,
            manualReloadFlow = requestReload,
        ) {
            getAccountData()
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    private val existingConversations: StateFlow<DataState<List<Conversation>>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        requestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService.getConversations(
                courseId,
                authToken,
                serverUrl
            )
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)


    /**
     * Load the conversation id associated with this username or userId. If none exists, we create a new conversation
     */
    val conversationId: StateFlow<DataState<Long?>> =
        flatMapLatest<String, String, DataState<List<Conversation>>, DataState<Account>, DataState<Long?>>(
            serverConfigurationService.serverUrl,
            accountService.authToken,
            existingConversations,
            accountData
        ) { serverUrl, authToken, existingConversationsDataState, accountDataDataState ->
            when (val combinedDataState =
                existingConversationsDataState join accountDataDataState) {
                is DataState.Success -> {
                    val (existingConversations, accountData) = combinedDataState.data
                    if (userIdentifier.matches(accountData)) {
                        // We cannot start a chat with ourselves
                        flowOf(DataState.Success(null))
                    } else {
                        val existingId = existingConversations
                            .filterIsInstance<OneToOneChat>()
                            .firstOrNull { conv -> userIdentifier.matches(conv.partner) }
                            ?.id

                        if (existingId != null) {
                            flowOf(DataState.Success(existingId))
                        } else {
                            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                                conversationService.createOneToOneConversation(
                                    courseId = courseId,
                                    partnerUserIdentifier = userIdentifier,
                                    authToken = authToken,
                                    serverUrl = serverUrl
                                )
                                    .bind { chat -> chat.id }
                            }
                        }
                    }
                }

                else -> flowOf(combinedDataState.bind { null })
            }
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly)

    fun requestReload() {
        requestReload.tryEmit(Unit)
    }
}