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
        private const val KEY_IS_PRIVATE = "is_private"
        private const val KEY_IS_ANNOUNCEMENT = "announcement"
        private const val KEY_IS_COURSE_WIDE = "course_wide"
    }

    val name: StateFlow<String> = savedStateHandle.getStateFlow(KEY_NAME, "")
    val description: StateFlow<String> = savedStateHandle.getStateFlow(KEY_DESCRIPTION, "")

    val isPublic: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_IS_PRIVATE, true)
    val isAnnouncement: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_IS_ANNOUNCEMENT, false)
    private val isCourseWide: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(KEY_IS_COURSE_WIDE, false)

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
                isPublic = isPublic.value,
                isAnnouncement = isAnnouncement.value,
                isCourseWide = isCourseWide.value,
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

    fun updatePublic(isPrivate: Boolean) {
        savedStateHandle[KEY_IS_PRIVATE] = isPrivate
    }

    fun updateAnnouncement(isAnnouncement: Boolean) {
        savedStateHandle[KEY_IS_ANNOUNCEMENT] = isAnnouncement
    }

    fun updateCourseWide(isCourseWide: Boolean) {
        savedStateHandle[KEY_IS_COURSE_WIDE] = isCourseWide
    }
}
