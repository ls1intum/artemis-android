package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.overview

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.onFailure
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.orNull
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.mapIsChannelNameIllegal
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.mapIsDescriptionOrTopicIllegal
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings.SettingsBaseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class ConversationSettingsViewModel(
    initialCourseId: Long,
    initialConversationId: Long,
    conversationService: ConversationService,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    networkStatusProvider: NetworkStatusProvider,
    accountDataService: AccountDataService,
    private val savedStateHandle: SavedStateHandle,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
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
        private const val KEY_NAME = "name"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_TOPIC = "topic"

        private const val TAG = "ConversationSettingsViewModel"
    }

    private val _name: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_NAME, null)
    private val _description: StateFlow<String?> =
        savedStateHandle.getStateFlow(KEY_DESCRIPTION, null)
    private val _topic: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_TOPIC, null)

    val isNameIllegal: StateFlow<Boolean> = _name
        .map { it.orEmpty() }
        .mapIsChannelNameIllegal()
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val isDescriptionIllegal: StateFlow<Boolean> = _description
        .map { it.orEmpty() }
        .mapIsDescriptionOrTopicIllegal()
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val isTopicIllegal: StateFlow<Boolean> = _topic
        .map { it.orEmpty() }
        .mapIsDescriptionOrTopicIllegal()
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    private val _onChannelDeleted = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onChannelDeleted = _onChannelDeleted.asSharedFlow()

    private val savedName = MutableStateFlow<String?>(null)
    private val savedDescription = MutableStateFlow<String?>(null)
    private val savedTopic = MutableStateFlow<String?>(null)

    val conversation: StateFlow<DataState<Conversation>> = combine(
        loadedConversation,
        savedName,
        savedDescription,
        savedTopic
    ) { convDataState, savedName, savedDescription, savedTopic ->
        convDataState.bind { conversation ->
            copyConversationWithSavedValues(conversation, savedName, savedDescription, savedTopic)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val previewMembers: StateFlow<DataState<List<ConversationUser>>> = flatMapLatest(
        conversationSettings,
        accountService.authToken,
        serverConfigurationService.serverUrl,
        onRequestReload.onStart { emit(Unit) }
    ) { conversationSettings, authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService.getMembers(
                courseId = conversationSettings.courseId,
                conversationId = conversationSettings.conversationId,
                query = "",
                size = 10,
                pageNum = 0,
                authToken = authToken,
                serverUrl = serverUrl
            )
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val isNameDirty = isDirtyFlow(_name) {
        when (this) {
            is ChannelChat -> name
            is GroupChat -> name
            is OneToOneChat -> null
        }
    }
    private val isDescriptionDirty = isDirtyFlow(_description) {
        when (this) {
            is ChannelChat -> description
            is GroupChat -> null
            is OneToOneChat -> null
        }
    }

    private val isTopicDirty = isDirtyFlow(_topic) {
        when (this) {
            is ChannelChat -> topic
            is GroupChat -> null
            is OneToOneChat -> null
        }
    }

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
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

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
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, "")

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
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, "")

    val topic: StateFlow<String> = combine(conversation, _topic) { conversation, customTopic ->
        customTopic ?: conversation.bind {
            when (it) {
                is ChannelChat -> it.topic.orEmpty()
                is GroupChat -> ""
                is OneToOneChat -> ""
            }
        }.orElse("")
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, "")

    val canEdit: StateFlow<Boolean> = conversation.map { conversationDataState ->
        conversationDataState.bind { conversation ->
            when (conversation) {
                is ChannelChat -> conversation.hasChannelModerationRights
                is GroupChat -> conversation.isMember
                is OneToOneChat -> false
            }
        }.orElse(false)
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val canSave: StateFlow<Boolean> =
        combine(isNameIllegal, isDescriptionIllegal, isTopicIllegal) { a, b, c ->
            !a && !b && !c
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    fun saveChanges(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val conversation = conversation.value.orNull() ?: return@async false

            val newName = _name.value
            val newDescription = _description.value
            val newTopic = _topic.value

            conversationService
                .updateConversation(
                    courseId = conversationSettings.value.courseId,
                    conversationId = conversationSettings.value.conversationId,
                    newName = newName,
                    newDescription = newDescription,
                    newTopic = newTopic,
                    conversation = conversation,
                    authToken = accountService.authToken.first(),
                    serverUrl = serverConfigurationService.serverUrl.first()
                )
                .onSuccess { success ->
                    if (success) {
                        savedName.value = newName
                        savedDescription.value = newDescription
                        savedTopic.value = newTopic
                    }
                }
                .or(false)
        }
    }

    fun leaveConversation(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            // Leaving the conversation is like kicking yourself
            kickMember(clientUsername.value.orNull() ?: return@async false).await()
        }
    }

    fun toggleChannelArchivation(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val conversation = conversation.value.orNull() as? ChannelChat
                ?: return@async false

            val result = if (conversation.isArchived) {
                conversationService.unarchiveChannel(
                    courseId = conversationSettings.value.courseId,
                    conversationId = conversationSettings.value.conversationId,
                    authToken = accountService.authToken.first(),
                    serverUrl = serverConfigurationService.serverUrl.first()
                )
            } else {
                conversationService.archiveChannel(
                    courseId = conversationSettings.value.courseId,
                    conversationId = conversationSettings.value.conversationId,
                    authToken = accountService.authToken.first(),
                    serverUrl = serverConfigurationService.serverUrl.first()
                )
            }
                .onFailure {
                    Log.d(TAG, "Could not perform channel archivation", it)
                }
                .or(false)

            if (result) {
                onRequestReload.tryEmit(Unit)
            }

            result
        }
    }

    fun updateName(name: String) {
        savedStateHandle[KEY_NAME] = name
    }

    fun updateDescription(description: String) {
        savedStateHandle[KEY_DESCRIPTION] = description
    }

    fun updateTopic(topic: String) {
        savedStateHandle[KEY_TOPIC] = topic
    }

    override fun requestReload() {
        super.requestReload()

        savedName.value = null
        savedDescription.value = null
        savedTopic.value = null
    }

    override fun updateConversation(courseId: Long, conversationId: Long) {
        super.updateConversation(courseId, conversationId)

        savedStateHandle[KEY_NAME] = null
        savedStateHandle[KEY_DESCRIPTION] = null
        savedStateHandle[KEY_TOPIC] = null

        savedName.value = null
        savedDescription.value = null
        savedTopic.value = null
    }

    private fun <T> isDirtyFlow(
        localValueFlow: Flow<T>,
        getCurrentValue: Conversation.() -> T
    ): Flow<Boolean> = combine(localValueFlow, conversation) { localValue, conversationDataState ->
        conversationDataState.bind(getCurrentValue).bind { localValue != null && it != localValue }
            .orElse(false)
    }

    private fun copyConversationWithSavedValues(
        conversation: Conversation,
        savedName: String?,
        savedDescription: String?,
        savedTopic: String?
    ) = when (conversation) {
        is ChannelChat -> {
            conversation
                .let {
                    if (savedName != null) {
                        it.copy(name = savedName)
                    } else it
                }
                .let {
                    if (savedDescription != null) {
                        it.copy(description = savedDescription)
                    } else it
                }
                .let {
                    if (savedTopic != null) {
                        it.copy(topic = savedTopic)
                    } else it
                }
        }

        is GroupChat -> {
            conversation
                .let {
                    if (savedName != null) {
                        it.copy(name = savedName)
                    } else it
                }
        }

        is OneToOneChat -> conversation
    }

    fun deleteConversation(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            val result =
                conversationService.deleteChannel(
                    courseId = conversationSettings.value.courseId,
                    conversationId = conversationSettings.value.conversationId,
                    authToken = accountService.authToken.first(),
                    serverUrl = serverConfigurationService.serverUrl.first()
                ).onFailure {
                    Log.d(TAG, "Could not delete channel", it)
                }.or(false)

            if (result) {
                _onChannelDeleted.tryEmit(Unit)
            }

            result
        }
    }
}
