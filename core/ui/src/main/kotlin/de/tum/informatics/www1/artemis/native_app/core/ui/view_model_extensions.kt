package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.authToken
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

fun ViewModel.authenticationStateFlow(accountService: AccountService): StateFlow<AccountService.AuthenticationData> =
    accountService.authenticationData.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AccountService.AuthenticationData.NotLoggedIn
    )

fun ViewModel.authTokenStateFlow(accountService: AccountService): StateFlow<String> =
    accountService.authToken.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ""
    )

fun ViewModel.serverUrlStateFlow(serverConfigurationService: ServerConfigurationService): StateFlow<String> =
    serverConfigurationService.serverUrl.stateIn(viewModelScope, SharingStarted.Eagerly, "")
