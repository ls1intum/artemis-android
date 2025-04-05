package de.tum.informatics.www1.artemis.native_app.feature.faq.ui.detail

import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.ui.serverUrlStateFlow
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.FaqRepository
import de.tum.informatics.www1.artemis.native_app.feature.faq.repository.data.Faq
import de.tum.informatics.www1.artemis.native_app.core.ui.ReloadableViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FaqDetailViewModel(
    courseId: Long,
    faqId: Long,
    private val faqRepository: FaqRepository,
    serverConfigurationService: ServerConfigurationService,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ReloadableViewModel() {
    val faq: StateFlow<DataState<Faq>> = requestReload.onStart { emit(Unit) }.flatMapLatest {
        faqRepository.getFaq(
            courseId = courseId,
            faqId = faqId,
        )
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val serverUrl: StateFlow<String> = serverUrlStateFlow(serverConfigurationService)

}