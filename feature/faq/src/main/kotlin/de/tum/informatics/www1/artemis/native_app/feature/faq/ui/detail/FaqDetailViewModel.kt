package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail

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
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FaqDetailViewModel(
    courseId: Long,
    faqId: Long,
    private val faqRepository: FaqRepository,
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)


    val faq: StateFlow<DataState<Faq>> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken,
    ) { serverUrl, authToken ->
        faqRepository.getFaq(
            courseId = courseId,
            faqId = faqId,
            authToken = authToken,
            serverUrl = serverUrl
        )
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    // TODO: create reloadableCourseBasedViewModel
    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}