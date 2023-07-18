package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.mapIsChannelNameIllegal
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.mapIsDescriptionOrTopicIllegal
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class CreateChannelViewModel(
    private val courseId: Long,
    private val conversationService: ConversationService,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val savedStateHandle: SavedStateHandle,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private companion object {
        private const val KEY_NAME = "name"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_IS_PUBLIC = "is_public"
        private const val KEY_IS_ANNOUNCEMENT = "announcement"
    }

    val name: StateFlow<String> = savedStateHandle.getStateFlow(KEY_NAME, "")
    val description: StateFlow<String> = savedStateHandle.getStateFlow(KEY_DESCRIPTION, "")

    val isPublic: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_IS_PUBLIC, true)
    val isAnnouncement: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_IS_ANNOUNCEMENT, true)

    val isNameIllegal: StateFlow<Boolean> = name
        .mapIsChannelNameIllegal()
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val isDescriptionIllegal: StateFlow<Boolean> = description
        .mapIsDescriptionOrTopicIllegal()
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    val canCreate: StateFlow<Boolean> =
        combine(isNameIllegal, isDescriptionIllegal, name) { isNameIllegal, isDescriptionIllegal, name ->
            !isNameIllegal && !isDescriptionIllegal && name.isNotEmpty()
        }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)

    fun createChannel(): Deferred<ChannelChat?> {
        return viewModelScope.async(coroutineContext) {
            val authToken = accountService.authToken.first()
            val serverUrl = serverConfigurationService.serverUrl.first()

            conversationService.createChannel(
                courseId = courseId,
                name = name.value,
                description = description.value,
                isPublic = isPublic.value,
                isAnnouncement = isAnnouncement.value,
                authToken = authToken,
                serverUrl = serverUrl
            ).orNull()
        }
    }

    fun updateName(name: String) {
        savedStateHandle[KEY_NAME] = name
    }

    fun updateDescription(description: String) {
        savedStateHandle[KEY_DESCRIPTION] = description
    }

    fun updatePublic(isPublic: Boolean) {
        savedStateHandle[KEY_IS_PUBLIC] = isPublic
    }

    fun updateAnnouncement(isAnnouncement: Boolean) {
        savedStateHandle[KEY_IS_ANNOUNCEMENT] = isAnnouncement
    }
}
