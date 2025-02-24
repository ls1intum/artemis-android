package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.getConversation
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

abstract class SettingsBaseViewModel(
    initialCourseId: Long,
    initialConversationId: Long,
    protected val conversationService: ConversationService,
    protected val accountService: AccountService,
    protected val serverConfigurationService: ServerConfigurationService,
    protected val networkStatusProvider: NetworkStatusProvider,
    accountDataService: AccountDataService,
    private val coroutineContext: CoroutineContext
) : ViewModel() {

    protected val conversationSettings = MutableStateFlow(ConversationSettings(initialCourseId, initialConversationId))

    protected val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    protected val loadedConversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        conversationSettings,
        accountService.authToken,
        serverConfigurationService.serverUrl,
        onRequestReload.onStart { emit(Unit) }
    ) { convSettings, authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService
                .getConversation(convSettings.courseId, convSettings.conversationId, authToken, serverUrl)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val clientUsername: StateFlow<DataState<String>> = flatMapLatest(
        onRequestReload.onStart { emit(Unit) },
        accountDataService.onReloadRequired,
    ) { _, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            accountDataService.getAccountData()
        }
            .map { accountDataState -> accountDataState.bind { it.username.orEmpty() } }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

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
        return viewModelScope.async(coroutineContext) {
            val conversation = loadedConversation.value.orNull() ?: return@async false

            conversationService.action(
                conversationSettings.value.courseId,
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

    open fun updateConversation(courseId: Long, conversationId: Long) {
        conversationSettings.value = ConversationSettings(courseId, conversationId)
    }

    protected data class ConversationSettings(val courseId: Long, val conversationId: Long)
}
