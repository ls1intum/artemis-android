package de.tum.informatics.www1.artemis.native_app.core.datastore.util

import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val AccountService.clientId: Flow<Int>
    get() = authenticationData.map { authData ->
        when (authData) {
            is AccountService.AuthenticationData.LoggedIn -> authData.account.bind { it.id }
                .orElse(null) ?: -1
            AccountService.AuthenticationData.NotLoggedIn -> -1
        }
    }