package de.tum.informatics.www1.artemis.native_app.core.datastore.impl

import android.util.Log
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
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
    scope: CoroutineScope = MainScope()
) : ArtemisContextProvider {

    private val _courseId = MutableStateFlow<Long?>(null)

    override val stateFlow: StateFlow<ArtemisContext> = combine(
        serverConfigurationService.serverUrl,
        accountService.authenticationData,
        _courseId
    ) { serverUrl, authData, courseId ->
        getArtemisContext(
            serverUrl = serverUrl,
            authData = authData,
            courseId = courseId
        )
    }.stateIn(scope, SharingStarted.Eagerly, ArtemisContextImpl.Empty)

    private fun getArtemisContext(
        serverUrl: String,
        authData: AccountService.AuthenticationData,
        courseId: Long?
    ): ArtemisContext {
        if (serverUrl.isEmpty()) {
            return ArtemisContextImpl.Empty
        }

        if (authData !is AccountService.AuthenticationData.LoggedIn) {
            return ArtemisContextImpl.ServerSelected(serverUrl)
        }

        if (courseId == null) {
            return ArtemisContextImpl.LoggedIn(
                serverUrl = serverUrl,
                authToken = authData.authToken,
                loginName = authData.username
            )
        }

        return ArtemisContextImpl.Course(
            serverUrl = serverUrl,
            authToken = authData.authToken,
            loginName = authData.username,
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