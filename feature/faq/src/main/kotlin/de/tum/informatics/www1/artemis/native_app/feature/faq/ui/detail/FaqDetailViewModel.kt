package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FaqDetailViewModel(
    faqId: Long,
    private val faqRepository: FaqRepository,
    serverConfigurationService: ServerConfigurationService,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val onRequestReload = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val faq: StateFlow<DataState<Faq>> = onRequestReload.onStart { emit(Unit) }.flatMapLatest {
        faqRepository.getFaq(
            faqId = faqId,
        )
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

    fun requestReload() {
        onRequestReload.tryEmit(Unit)
    }
}