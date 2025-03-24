package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.service.performAutoReloadingNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal class SinglePageConversationBodyViewModel(
    courseId: Long,
    courseService: CourseService,
    accountDataService: AccountDataService,
    networkStatusProvider: NetworkStatusProvider,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    val canCreateChannel: StateFlow<Boolean> = courseService.performAutoReloadingNetworkCall(
        networkStatusProvider = networkStatusProvider
    ) {
        courseService.getCourse(courseId)
            .then { courseWithScore ->
                accountDataService
                    .getAccountData()
                    .bind { it.isAtLeastTutorInCourse(courseWithScore.course) }
            }
    }
        .map { it.orElse(false) }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly, false)
}