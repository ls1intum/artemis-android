package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.mapIsChannelNameIllegal
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common.mapIsDescriptionOrTopicIllegal
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class CreateChannelViewModel(
    private val courseId: Long,
    private val conversationService: de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService,
    private val accountService: AccountService,
    private val serverConfigurationService: ServerConfigurationService,
    private val savedStateHandle: SavedStateHandle,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private companion object {
        private const val KEY_NAME = "name"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_VISIBILITY = "is_public"
        private const val KEY_IS_ANNOUNCEMENT = "announcement"
        private const val KEY_SCOPE = "course_wide"
    }

    enum class Scope {
        COURSEWIDE,
        SELECTIVE,
    }

    enum class Visibility {
        PUBLIC,
        PRIVATE
    }

    val name: StateFlow<String> = savedStateHandle.getStateFlow(KEY_NAME, "")
    val description: StateFlow<String> = savedStateHandle.getStateFlow(KEY_DESCRIPTION, "")

    val visibility: StateFlow<Visibility> = savedStateHandle.getStateFlow(KEY_VISIBILITY, Visibility.PUBLIC)
    val isAnnouncement: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_IS_ANNOUNCEMENT, false)
    val scope: StateFlow<Scope> =
        savedStateHandle.getStateFlow(KEY_SCOPE, Scope.COURSEWIDE)

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

    fun createChannel(onChannelCreated: (ChannelChat?) -> Unit) {
        viewModelScope.launch(coroutineContext) {
            val authToken = accountService.authToken.first()
            val serverUrl = serverConfigurationService.serverUrl.first()

            val channel = conversationService.createChannel(
                courseId = courseId,
                name = name.value,
                description = description.value,
                isPublic = visibility.value == Visibility.PUBLIC,
                isAnnouncement = isAnnouncement.value,
                isCourseWide = scope.value == Scope.COURSEWIDE,
                authToken = authToken,
                serverUrl = serverUrl
            ).orNull()

            onChannelCreated(channel)
        }
    }

    fun updateName(name: String) {
        savedStateHandle[KEY_NAME] = name
    }

    fun updateDescription(description: String) {
        savedStateHandle[KEY_DESCRIPTION] = description
    }

    fun updateVisibility(visibility: Visibility) {
        savedStateHandle[KEY_VISIBILITY] = visibility
    }

    fun updateAnnouncement(isAnnouncement: Boolean) {
        savedStateHandle[KEY_IS_ANNOUNCEMENT] = isAnnouncement
    }

    fun updateScope(scope: Scope) {
        savedStateHandle[KEY_SCOPE] = scope
    }
}