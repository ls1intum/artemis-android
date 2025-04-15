package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.CourseUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.service.network.ConversationService
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.InclusionList
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.MemberSelectionItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.member_selection.util.toMemberSelectionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlinx.parcelize.Parcelize
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

abstract class MemberSelectionBaseViewModel(
    protected val courseId: Long,
    protected val conversationService: ConversationService,
    protected val accountService: AccountService,
    protected val serverConfigurationService: ServerConfigurationService,
    protected val networkStatusProvider: NetworkStatusProvider,
    private val savedStateHandle: SavedStateHandle,
    coroutineContext: CoroutineContext
) : ReloadableViewModel() {

    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_INCLUSION_LIST = "inclusion_list"
        private const val KEY_MEMBER_ITEMS= "member_items"

        val QUERY_DEBOUNCE_TIME = 200.milliseconds

        /** The minimum length of the query to trigger a search */
        const val MINIMUM_QUERY_LENGTH = 3
    }

    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    val isQueryTooShort: StateFlow<Boolean> =
        query.debounce(QUERY_DEBOUNCE_TIME).map { it.length < MINIMUM_QUERY_LENGTH }
            .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, true)

    val inclusionList: StateFlow<InclusionList> =
        savedStateHandle.getStateFlow(KEY_INCLUSION_LIST, InclusionList())

    private val _memberItems =
        savedStateHandle.getStateFlow(KEY_MEMBER_ITEMS, MemberItemsList(emptyList(), emptyList()))

    val recipients: StateFlow<List<MemberSelectionItem.Recipient>> = _memberItems
        .map { it.recipients }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    val conversations: StateFlow<List<MemberSelectionItem.Conversation>> = _memberItems
        .map { it.conversations }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    val memberItems: StateFlow<List<MemberSelectionItem>> = _memberItems
        .map { it.conversations + it.recipients }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, emptyList())

    private val recipientsFromServer: Flow<DataState<List<CourseUser>>> = flatMapLatest(
        query.debounce(QUERY_DEBOUNCE_TIME),
        isQueryTooShort,
        inclusionList,
        accountService.authToken,
        serverConfigurationService.serverUrl,
        requestReload.onStart { emit(Unit) }
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

    val potentialRecipients: StateFlow<DataState<List<CourseUser>>> = combine(
        recipientsFromServer,
        recipients
    ) { recipientsFromServerDataState, recipients ->
        val recipientsAsUsernames = recipients.map { it.username }.toHashSet()

        recipientsFromServerDataState.bind { recipientsFromServer ->
            recipientsFromServer.filter { it.username !in recipientsAsUsernames }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val conversationsFromServer: StateFlow<DataState<List<Conversation>>> = combine(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        requestReload.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            conversationService.getConversations(courseId, authToken, serverUrl).bind { conversations ->
                // For forwarding a post we currently only allow channels
                conversations.filter { it is ChannelChat && !it.isAnnouncementChannel }
            }
        }
    }
        .flattenMerge()
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    private val potentialConversations: StateFlow<DataState<List<Conversation>>> = combine(
        query.debounce(QUERY_DEBOUNCE_TIME),
        conversationsFromServer,
        conversations
    ) { query, conversationsFromServerDataState, conversations ->
        val conversationIds = conversations.map { it.id }

        conversationsFromServerDataState.bind { conversationsFromServer ->
            conversationsFromServer.filter {
                it.humanReadableName.contains(
                    query,
                    ignoreCase = true
                ) && it.id !in conversationIds
            }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    fun suggestions(isConversationSelectionEnabled: Boolean): StateFlow<List<MemberSelectionItem>> = combine(
        potentialConversations,
        potentialRecipients
    ) { potentialConversationsDataState, potentialRecipientsDataState ->
        val recipients = potentialRecipientsDataState.bind { courseUsers ->
            courseUsers.map { it.toMemberSelectionItem() }
        }.orElse(emptyList())

        if (isConversationSelectionEnabled) {
            val conversations = potentialConversationsDataState.bind { conversations ->
                conversations.map { it.toMemberSelectionItem() }
            }.orElse(emptyList())

            conversations + recipients
        } else {
            recipients
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
    }

    fun addMemberItem(item: MemberSelectionItem) {
        when (item) {
            is MemberSelectionItem.Recipient -> {
                savedStateHandle[KEY_MEMBER_ITEMS] = MemberItemsList(
                    conversations.value, recipients.value + item
                )
            }
            is MemberSelectionItem.Conversation -> {
                savedStateHandle[KEY_MEMBER_ITEMS] = MemberItemsList(
                    conversations.value + item, recipients.value
                )
            }
        }
    }

    fun removeMemberItem(item: MemberSelectionItem) {
        when (item) {
            is MemberSelectionItem.Conversation -> {
                savedStateHandle[KEY_MEMBER_ITEMS] = MemberItemsList(
                    conversations.value - item, recipients.value
                )
            }
            is MemberSelectionItem.Recipient -> {
                savedStateHandle[KEY_MEMBER_ITEMS] = MemberItemsList(
                    conversations.value, recipients.value - item
                )
            }
        }
    }

    fun updateInclusionList(inclusionList: InclusionList) {
        savedStateHandle[KEY_INCLUSION_LIST] = inclusionList
    }

    protected fun reset() {
        savedStateHandle[KEY_MEMBER_ITEMS] = MemberItemsList(
            emptyList(),
            emptyList()
        )
        savedStateHandle[KEY_INCLUSION_LIST] = InclusionList()
        savedStateHandle[KEY_QUERY] = ""
    }

    @Parcelize
    private data class MemberItemsList(val conversations: List<MemberSelectionItem.Conversation>, val recipients: List<MemberSelectionItem.Recipient>) : Parcelable
}