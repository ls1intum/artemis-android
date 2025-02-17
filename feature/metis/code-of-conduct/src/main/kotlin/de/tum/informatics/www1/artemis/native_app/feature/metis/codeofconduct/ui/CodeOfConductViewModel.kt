package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.common.flatMapLatest
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.retryOnInternet
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.CourseService
import de.tum.informatics.www1.artemis.native_app.core.data.stateIn
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import de.tum.informatics.www1.artemis.native_app.core.device.NetworkStatusProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.CodeOfConductService
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.service.CodeOfConductStorageService
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * This implementation does the following: It fetches the code of conduct of the course, if the course
 * CoC is blank, we instead fetch the template code of conduct. However, we need to store locally if we have
 * accepted the code of conduct, for which we use [CodeOfConductStorageService].
 */
internal class CodeOfConductViewModel(
    private val courseId: Long,
    private val codeOfConductService: CodeOfConductService,
    private val codeOfConductStorageService: CodeOfConductStorageService,
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
    private val hasNetworkCodeOfConductBeenAcceptedClient = MutableStateFlow(false)

    /**
     * Holds the code of conduct and information if it is the course coc or the one from the template
     */
    private val codeOfConductInternalState: StateFlow<DataState<CodeOfConductState>> =
        flatMapLatest(
            accountService.authToken,
            serverConfigurationService.serverUrl,
            courseService.onReloadRequired,
            requestReload.onStart { emit(Unit) }
        ) { authToken, serverUrl, _, _ ->
            retryOnInternet(networkStatusProvider.currentNetworkStatus) {
                /*
                First fetch the coc of the course itself, if it is empty, load the template instead.
                 */
                courseService
                    .getCourse(courseId)
                    .bind { it.course.courseInformationSharingMessagingCodeOfConduct }
                    .then { courseCoc ->
                        if (courseCoc.isBlank()) {
                            codeOfConductService.getCodeOfConductTemplate(
                                courseId,
                                serverUrl,
                                authToken
                            ).bind { CodeOfConductState(it, isTemplate = true) }
                        } else {
                            NetworkResponse.Response(
                                CodeOfConductState(
                                    courseCoc,
                                    isTemplate = false
                                )
                            )
                        }
                    }
            }
        }
            // Delay until actually needed
            .stateIn(viewModelScope, SharingStarted.Lazily)

    val codeOfConduct: StateFlow<DataState<String>> = codeOfConductInternalState
        .map { internalState -> internalState.bind { it.codeOfConduct } }
        .stateIn(viewModelScope, SharingStarted.Lazily)

    /**
     * If we have stored locally that we have accepted the template code of conduct
     */
    private val isCodeOfConductAcceptedLocally: StateFlow<DataState<Boolean>> = flatMapLatest(
        codeOfConduct,
        serverConfigurationService.host
    ) { cocDataState, host ->
        when (cocDataState) {
            is DataState.Success -> codeOfConductStorageService.isCodeOfConductAccepted(
                host,
                courseId,
                cocDataState.data
            ).map { DataState.Success(it) }

            is DataState.Failure, is DataState.Loading -> flowOf(cocDataState.bind { false })
        }
    }
        .stateIn(viewModelScope, SharingStarted.Lazily)

    val isCodeOfConductAccepted: StateFlow<DataState<Boolean>> = combine(
        codeOfConductInternalState,
        isCodeOfConductAcceptedNetwork,
        hasNetworkCodeOfConductBeenAcceptedClient,
        isCodeOfConductAcceptedLocally
    ) { cocStateDataState, isCodeOfConductAccepted, hasNetworkCodeOfConductBeenAccepted, isCodeOfConductAcceptedLocally ->
        cocStateDataState.transform { cocState ->
            when {
                cocState.isTemplate -> isCodeOfConductAcceptedLocally
                hasNetworkCodeOfConductBeenAccepted -> DataState.Success(true)
                else -> isCodeOfConductAccepted
            }
        }
    }
        .stateIn(viewModelScope + coroutineContext, SharingStarted.Eagerly)

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
            val state = codeOfConductInternalState.value
            if (state !is DataState.Success) return@async false

            if (state.data.isTemplate) {
                codeOfConductStorageService
                    .acceptCodeOfConduct(
                        serverHost = serverConfigurationService.host.first(),
                        courseId = courseId,
                        codeOfConduct = state.data.codeOfConduct
                    )

                true
            } else {
                codeOfConductService.acceptCodeOfConduct(
                    courseId = courseId,
                    serverUrl = serverConfigurationService.serverUrl.first(),
                    authToken = accountService.authToken.first()
                )
                    .onSuccess {
                        hasNetworkCodeOfConductBeenAcceptedClient.value = true
                    }
                    .bind { true }
                    .or(false)
            }
        }
    }

    fun requestReload() {
        requestReload.tryEmit(Unit)
    }

    private data class CodeOfConductState(val codeOfConduct: String, val isTemplate: Boolean)
}
