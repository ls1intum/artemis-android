package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.Course
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.CodeOfConductService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class CodeOfConductViewModel(
    private val courseId: Long,
    private val codeOfConductService: CodeOfConductService,
    private val networkStatusProvider: NetworkStatusProvider,
    private val serverConfigurationService: ServerConfigurationService,
    private val accountService: AccountService,
    private val courseService: CourseService,
    private val coroutineContext: CoroutineContext = EmptyCoroutineContext
) : ViewModel() {

    private val requestReload = MutableSharedFlow<Unit>()

    /**
     * The status last asked from the server
     */
    private val isCodeOfConductAcceptedNetwork: StateFlow<DataState<Boolean>> = flatMapLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        requestReload.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            codeOfConductService.getIsCodeOfConductAccepted(courseId, serverUrl, authToken)
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    /**
     * Set to true if the user clicked accept and the server responded with success
     */
    private val hasCodeOfConductBeenAcceptedClient = MutableStateFlow(false)

    val isCodeOfConductAccepted: StateFlow<DataState<Boolean>> = combine(
        isCodeOfConductAcceptedNetwork,
        hasCodeOfConductBeenAcceptedClient
    ) { isCodeOfConductAccepted, hasCodeOfConductBeenAccepted ->
        if (hasCodeOfConductBeenAccepted) {
            DataState.Success(true)
        } else {
            isCodeOfConductAccepted
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

    val codeOfConduct: StateFlow<DataState<String>> = flatMapLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        requestReload.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            courseService
                .getCourse(courseId, serverUrl, authToken)
                .bind { it.course.courseInformationSharingMessagingCodeOfConduct }
        }
    }
        // Delay until actually needed
        .stateIn(viewModelScope, SharingStarted.Lazily)

    val responsibleUsers: StateFlow<DataState<List<User>>> = flatMapLatest(
        accountService.authToken,
        serverConfigurationService.serverUrl,
        requestReload.onStart { emit(Unit) }
    ) { authToken, serverUrl, _ ->
        retryOnInternet(networkStatusProvider.currentNetworkStatus) {
            codeOfConductService.getResponsibleUsers(courseId, serverUrl, authToken)
        }
    }
        // Delay until actually needed
        .stateIn(viewModelScope, SharingStarted.Lazily)

    fun acceptCodeOfConduct(): Deferred<Boolean> {
        return viewModelScope.async(coroutineContext) {
            codeOfConductService.acceptCodeOfConduct(
                courseId = courseId,
                serverUrl = serverConfigurationService.serverUrl.first(),
                authToken = accountService.authToken.first()
            )
                .onSuccess {
                    hasCodeOfConductBeenAcceptedClient.value = true
                }
                .bind { true }
                .or(false)
        }
    }

    fun requestReload() {
        requestReload.tryEmit(Unit)
    }
}