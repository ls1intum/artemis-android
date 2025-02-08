package de.tum.informatics.www1.artemis.native_app.feature.faq.ui

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FaqOverviewViewModel(
    courseId: Long,
    private val faqRepository: FaqRepository,
    serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val faqs: StateFlow<DataState<List<Faq>>> = flatMapLatest(
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


    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}