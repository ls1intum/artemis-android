package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.member_selection

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
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.create_personal_conversation.InclusionList
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
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
import kotlinx.coroutines.plus
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

internal abstract class MemberSelectionBaseViewModel(
    protected val courseId: Long,
    protected val conversationService: ConversationService,
    protected val accountService: AccountService,
    protected val serverConfigurationService: ServerConfigurationService,
    protected val networkStatusProvider: NetworkStatusProvider,
    private val savedStateHandle: SavedStateHandle,
    coroutineContext: CoroutineContext
) : ViewModel() {

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_INCLUSION_LIST = "inclusion_list"
        private const val KEY_RECIPIENTS = "recipients"

        val QUERY_DEBOUNCE_TIME = 200.milliseconds

        /** The minimum length of the query to trigger a search */
        const val MINIMUM_QUERY_LENGTH = 3
    }

    private val onRequestReloadPotentialRecipients =
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val isQueryTooShort: StateFlow<Boolean> = query.map { it.length < MINIMUM_QUERY_LENGTH }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, true)

    val inclusionList: StateFlow<InclusionList> =
        savedStateHandle.getStateFlow(KEY_INCLUSION_LIST, InclusionList())

    private val _recipients =
        savedStateHandle.getStateFlow(KEY_RECIPIENTS, RecipientsList(emptyList()))

    val recipients: StateFlow<List<Recipient>> = _recipients
        .map { it.recipients }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    private val recipientsFromServer: Flow<DataState<List<User>>> = flatMapLatest(
        query.debounce(QUERY_DEBOUNCE_TIME),
        isQueryTooShort,
        inclusionList,
        accountService.authToken,
        serverConfigurationService.serverUrl,
        onRequestReloadPotentialRecipients.onStart { emit(Unit) }
    ) { query, isQueryToShort, inclusionList, authToken, serverUrl, _ ->
        if (isQueryToShort) {
            flowOf(DataState.Success(emptyList()))
        } else {
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
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val potentialRecipients: StateFlow<DataState<List<User>>> = combine(
        recipientsFromServer,
        recipients
    ) { recipientsFromServerDataState, recipients ->
        val recipientsAsUsernames = recipients.map { it.username }.toHashSet()

        recipientsFromServerDataState.bind { recipientsFromServer ->
            recipientsFromServer.filter { it.username !in recipientsAsUsernames }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

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

    protected fun reset() {
        savedStateHandle[KEY_RECIPIENTS] = RecipientsList(emptyList())
        savedStateHandle[KEY_INCLUSION_LIST] = InclusionList()
        savedStateHandle[KEY_QUERY] = ""
    }

    @Parcelize
    private data class RecipientsList(val recipients: List<Recipient>) : Parcelable
}