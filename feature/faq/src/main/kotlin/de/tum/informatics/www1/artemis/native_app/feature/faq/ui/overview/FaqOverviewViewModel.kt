package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FaqOverviewViewModel(
    courseId: Long,
    private val faqRepository: FaqRepository,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val allFaqs: StateFlow<DataState<List<Faq>>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
        onRequestReload.onStart { emit(Unit) },
    ) { serverUrl, authToken, _ ->
        faqRepository.getFaqs(
            courseId = courseId,
            authToken = authToken,
            serverUrl = serverUrl
        )
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val _searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val displayedFaqs: StateFlow<DataState<List<Faq>>> = combine(
        allFaqs,
        searchQuery
    ) { faqsDataState, query ->
        if (query.isBlank()) {
            return@combine faqsDataState
        }

        faqsDataState.bind { faqs ->
            faqs.filter {
                it.questionTitle.contains(query, ignoreCase = true) ||
                it.questionAnswer.contains(query, ignoreCase = true)
            }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)


    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }

    fun updateQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }
}