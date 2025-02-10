package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.isAtLeastTutorInCourse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class SinglePageConversationBodyViewModel(
    courseId: Long,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    courseService: CourseService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    val canCreateChannel: StateFlow<Boolean> = flatMapLatest(
        serverConfigurationService.serverUrl,
        accountService.authToken
    ) { serverUrl, authToken ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            courseService.getCourse(courseId, serverUrl, authToken)
                .then { courseWithScore ->
                    accountDataService
                        .getAccountData()
                        .bind { it.isAtLeastTutorInCourse(courseWithScore.course) }
                }
        }.map { it.orElse(false) }
    }.stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)
}