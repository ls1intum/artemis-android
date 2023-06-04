package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.member_selection

import android.os.Parcelable
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
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation.InclusionList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.humanReadableName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.parcelize.Parcelize

internal abstract class MemberSelectionBaseViewModel(
    protected val courseId: Long,
    protected val conversationService: ConversationService,
    protected val accountService: AccountService,
    protected val serverConfigurationService: ServerConfigurationService,
    protected val networkStatusProvider: NetworkStatusProvider,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_INCLUSION_LIST = "inclusion_list"
        private const val KEY_RECIPIENTS = "recipients"
    }

    protected val onRequestReloadPotentialRecipients =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val inclusionList: StateFlow<InclusionList> =
        savedStateHandle.getStateFlow(KEY_INCLUSION_LIST, InclusionList())

    private val _recipients =
        savedStateHandle.getStateFlow(KEY_RECIPIENTS, RecipientsList(emptyList()))

    val recipients: StateFlow<List<Recipient>> = _recipients
        .map { it.recipients }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val recipientsFromServer: Flow<DataState<List<User>>> = flatMapLatest(
        query.debounce(200),
        inclusionList,
        accountService.authToken,
        serverConfigurationService.serverUrl,
        onRequestReloadPotentialRecipients.onStart { emit(Unit) }
    ) { query, inclusionList, authToken, serverUrl, _ ->
        if (query.length >= 3) {
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                conversationService.searchForPotentialCommunicationParticipants(
                    courseId = courseId,
                    query = query.lowercase(),
                    includeStudents = inclusionList.students,
                    includeTutors = inclusionList.tutors,
                    includeInstructors = inclusionList.instructors,
                    serverUrl = serverUrl,
                    authToken = authToken
                )
            }
        } else flowOf(DataState.Success(emptyList()))
    }

    val potentialRecipients: StateFlow<DataState<List<User>>> = combine(
        recipientsFromServer,
        recipients
    ) { recipientsFromServerDataState, recipients ->
        val recipientsAsUsernames = recipients.map { it.username }.toHashSet()

        recipientsFromServerDataState.bind { recipientsFromServer ->
            recipientsFromServer.filter { it.username !in recipientsAsUsernames }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    fun updateQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
    }

    fun addRecipient(recipient: User) {
        savedStateHandle[KEY_RECIPIENTS] = RecipientsList(
            recipients.value + Recipient(
                recipient.username ?: return,
                recipient.humanReadableName
            )
        )
    }

    fun removeRecipient(recipient: Recipient) {
        savedStateHandle[KEY_RECIPIENTS] = RecipientsList(
            recipients.value - recipient
        )
    }

    fun updateInclusionList(inclusionList: InclusionList) {
        savedStateHandle[KEY_INCLUSION_LIST] = inclusionList
    }

    fun retryLoadPotentialRecipients() {
        onRequestReloadPotentialRecipients.tryEmit(Unit)
    }

    @Parcelize
    private data class RecipientsList(val recipients: List<Recipient>) : Parcelable
}