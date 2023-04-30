package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.create_personal_conversation

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
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.MetisService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart

internal class CreatePersonalConversationViewModel(
    private val courseId: Long,
    private val conversationService: ConversationService,
    networkStatusProvider: NetworkStatusProvider,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService
) : ViewModel() {

    private val onRequestReloadPotentialRecipients =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _inclusionList = MutableStateFlow(InclusionList())
    val inclusionList: StateFlow<InclusionList> = _inclusionList

    private val _recipients = MutableStateFlow<List<User>>(emptyList())
    val recipients: StateFlow<List<User>> = _recipients

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
        recipientsFromServerDataState.bind { recipientsFromServer ->
            recipientsFromServer.filter { it !in recipients }
        }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly)

    fun updateQuery(query: String) {
        _query.value = query
    }

    fun addRecipient(recipient: User) {
        _recipients.value += recipient
    }

    fun removeRecipient(recipient: User) {
        _recipients.value -= recipient
    }

    fun updateInclusionList(inclusionList: InclusionList) {
        _inclusionList.value = inclusionList
    }

    fun retryLoadPotentialRecipients() {
        onRequestReloadPotentialRecipients.tryEmit(Unit)
    }
}
