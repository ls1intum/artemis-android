package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

internal class ConversationSettingsViewModel(
    val courseId: Long,
    val conversationId: Long,
    private val conversationService: ConversationService,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_NAME = "name"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_TOPIC = "topic"
    }

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _name: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_NAME, null)
    private val _description: StateFlow<String?> =
        savedStateHandle.getStateFlow(KEY_DESCRIPTION, null)
    private val _topic: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_TOPIC, null)

    private val savedName = MutableStateFlow<String?>(null)
    private val savedDescription = MutableStateFlow<String?>(null)
    private val savedTopic = MutableStateFlow<String?>(null)

    private val isNameDirty = combine(_name, savedName) { n, sn -> n != null && n != sn }
    private val isDescriptionDirty =
        combine(_description, savedDescription) { n, sn -> n != null && n != sn }
    private val isTopicDirty = combine(_topic, savedTopic) { n, sn -> n != null && n != sn }

    /**
     * If the user has made changes we can sync with the server.
     */
    val isDirty: StateFlow<Boolean> =
        combine(
            isNameDirty,
            isDescriptionDirty,
            isTopicDirty
        ) { name, description, topic ->
            name || description || topic
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val conversation: StateFlow<DataState<Conversation>> = flatMapLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        onRequestReload.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService
                .getConversations(courseId, authToken, serverUrl)
                .bind { conversations ->
                    conversations.firstOrNull { it.id == conversationId }
                }
        }
            .map { dataState ->
                dataState.transform {
                    if (it != null) {
                        DataState.Success(it)
                    } else {
                        DataState.Failure(RuntimeException("Conversation not found"))
                    }
                }
            }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    /**
     * If no custom name is set displays the name of the conversation.
     * In the case of a group chat that has no name, an empty string is returned.
     */
    val name: StateFlow<String> = combine(conversation, _name) { conversation, customName ->
        customName ?: conversation.bind {
            when (it) {
                is ChannelChat -> it.name
                is GroupChat -> it.name.orEmpty()
                is OneToOneChat -> ""
            }
        }.orElse("")
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val description: StateFlow<String> =
        combine(conversation, _description) { conversation, customDescription ->
            customDescription ?: conversation.bind {
                when (it) {
                    is ChannelChat -> it.description.orEmpty()
                    is GroupChat -> ""
                    is OneToOneChat -> ""
                }
            }.orElse("")
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val topic: StateFlow<String> = combine(conversation, _topic) { conversation, customTopic ->
        customTopic ?: conversation.bind {
            when (it) {
                is ChannelChat -> it.topic.orEmpty()
                is GroupChat -> ""
                is OneToOneChat -> ""
            }
        }.orElse("")
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun updateName(name: String) {
        savedStateHandle[KEY_NAME] = name
    }

    fun updateDescription(description: String) {
        savedStateHandle[KEY_DESCRIPTION] = description
    }

    fun updateTopic(topic: String) {
        savedStateHandle[KEY_TOPIC] = topic
    }

    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}
