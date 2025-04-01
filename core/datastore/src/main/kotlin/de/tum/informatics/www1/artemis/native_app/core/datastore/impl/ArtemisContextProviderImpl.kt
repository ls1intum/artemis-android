package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArtemisContextProviderImpl(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    scope: CoroutineScope = MainScope()
) : ArtemisContextProvider {

    private val _stateFlow: MutableStateFlow<ArtemisContext> = MutableStateFlow(ArtemisContextImpl.Empty)
    override val stateFlow: StateFlow<ArtemisContext> = _stateFlow

    init {
        scope.launch {
            collectServerUrl(serverConfigurationService)
        }

        scope.launch {
            collectLoggedInState(accountService)
        }
    }

    private suspend fun collectServerUrl(serverConfigurationService: ServerConfigurationService) {
        serverConfigurationService.serverUrl.collectLatest {
            _stateFlow.value = ArtemisContextImpl.ServerSelected(it)
        }
    }

    private suspend fun collectLoggedInState(accountService: AccountService) {
        accountService.authenticationData.collectLatest { authData ->
            when (authData) {
                AccountService.AuthenticationData.NotLoggedIn ->
                    _stateFlow.value = ArtemisContextImpl.Empty
                is AccountService.AuthenticationData.LoggedIn ->
                    _stateFlow.value = ArtemisContextImpl.LoggedIn(
                        serverUrl = _stateFlow.value.serverUrl,
                        authToken = authData.authToken,
                        username = authData.username
                    )
            }
        }
    }

    override fun setCourseId(courseId: Long) {
        val currentContext = _stateFlow.value
        if (currentContext is ArtemisContext.LoggedIn) {
            _stateFlow.value = ArtemisContextImpl.Course(
                serverUrl = currentContext.serverUrl,
                authToken = currentContext.authToken,
                username = currentContext.username,
                courseId = courseId
            )
        }
    }

    override fun resetCourseId() {
        val currentContext = _stateFlow.value
        if (currentContext is ArtemisContext.Course) {
            _stateFlow.value = ArtemisContextImpl.LoggedIn(
                serverUrl = currentContext.serverUrl,
                authToken = currentContext.authToken,
                username = currentContext.username
            )
        }
    }
}