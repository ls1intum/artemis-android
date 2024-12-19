package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal class NavigateToUserConversationViewModel(
    private val courseId: Long,
    private val username: String,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    conversationService: ConversationService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider
) : ViewModel() {

    private val requestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val accountData: StateFlow<DataState<Account>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        requestReload.onStart { emit(Unit) }
    ) { serverUrl, authToken, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            accountDataService.getAccountData(serverUrl, authToken)
        }
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
     * Load the conversation id associated with this username. If none exists, we create a new conversatin
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
                    if (accountData.username == username) {
                        // We cannot start a chat with ourselves
                        flowOf(DataState.Success(null))
                    } else {
                        val existingId = existingConversations
                            .filterIsInstance<OneToOneChat>()
                            .firstOrNull { conv -> conv.members.any { it.username == username } }
                            ?.id

                        if (existingId != null) {
                            flowOf(DataState.Success(existingId))
                        } else {
                            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                                conversationService
                                    .createOneToOneConversation(
                                        courseId,
                                        username,
                                        authToken,
                                        serverUrl
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

@Composable
internal fun NavigateToUserConversationUi(
    modifier: Modifier,
    courseId: Long,
    username: String,
    onNavigateToConversation: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: NavigateToUserConversationViewModel =
        koinViewModel { parametersOf(courseId, username) }

    val conversationIdDataState by viewModel.conversationId.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            })
        }
    ) { padding ->
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = conversationIdDataState,
            loadingText = stringResource(id = R.string.navigate_to_user_conversation_ui_loading),
            failureText = stringResource(id = R.string.navigate_to_user_conversation_ui_failed),
            retryButtonText = stringResource(id = R.string.navigate_to_user_conversation_ui_try_again),
            onClickRetry = viewModel::requestReload
        ) { conversationId ->
            LaunchedEffect(key1 = conversationId) {
                if (conversationId != null) {
                    onNavigateToConversation(conversationId)
                } else {
                    // Username is the client itself. Fallback to overview.
                    onNavigateBack()
                }
            }
        }
    }
}