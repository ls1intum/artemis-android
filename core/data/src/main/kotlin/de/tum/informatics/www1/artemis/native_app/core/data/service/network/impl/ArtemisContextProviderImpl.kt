package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

const val TAG = "ArtemisContextProviderImpl"

class ArtemisContextProviderImpl(
    serverConfigurationService: ServerConfigurationService,
    accountService: AccountService,
    accountDataService: AccountDataService,
    scope: CoroutineScope = MainScope()
) : ArtemisContextProvider {

    private val _courseId = MutableStateFlow<Long?>(null)

    override val stateFlow: StateFlow<ArtemisContext> = combine(
        serverConfigurationService.serverUrl,
        accountService.authenticationData,
        accountDataService.accountDataFlow,
        _courseId
    ) { serverUrl, authData, accountData, courseId ->
        val newContext = getArtemisContext(
            serverUrl = serverUrl,
            authData = authData,
            account = accountData,
            courseId = courseId
        )
        Log.d(TAG, "Set new artemisContext: $newContext")
        newContext
    }.stateIn(scope, SharingStarted.Eagerly, ArtemisContextImpl.Empty)

    private fun getArtemisContext(
        serverUrl: String,
        authData: AccountService.AuthenticationData,
        account: Account?,
        courseId: Long?
    ): ArtemisContext {
        if (serverUrl.isEmpty()) {
            return ArtemisContextImpl.Empty
        }

        if (authData !is AccountService.AuthenticationData.LoggedIn) {
            return ArtemisContextImpl.ServerSelected(serverUrl)
        }

        if (account == null) {
            return ArtemisContextImpl.LoggedIn(
                serverUrl = serverUrl,
                authToken = authData.authToken,
                loginName = authData.username
            )
        }

        if (courseId == null) {
            return ArtemisContextImpl.AccountLoaded(
                serverUrl = serverUrl,
                authToken = authData.authToken,
                loginName = authData.username,
                account = account
            )
        }

        return ArtemisContextImpl.Course(
            serverUrl = serverUrl,
            authToken = authData.authToken,
            loginName = authData.username,
            account = account,
            courseId = courseId
        )
    }


    override fun setCourseId(courseId: Long) {
        Log.d(TAG, "Setting the courseId of the ArtemisContext to $courseId")
        _courseId.value = courseId
    }

    override fun clearCourseId() {
        Log.d(TAG, "Clearing the courseId of the ArtemisContext")
        _courseId.value = null
    }
}