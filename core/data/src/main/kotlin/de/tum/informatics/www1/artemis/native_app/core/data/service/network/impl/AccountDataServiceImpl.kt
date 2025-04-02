package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.artemis_context.impl.ArtemisContextImpl
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException

internal class AccountDataServiceImpl(
    private val context: Context,
    private val ktorProvider: KtorProvider,
    accountService: AccountService,
    serverConfigurationService: ServerConfigurationService,
    private val jsonProvider: JsonProvider,
) : AccountDataService {

    private val serverUrlFlow: Flow<String> = serverConfigurationService.serverUrl
    private val authDataFlow: Flow<AccountService.AuthenticationData> = accountService.authenticationData

    override val onArtemisContextChanged: Flow<ArtemisContext.LoggedIn> = combine(
        serverUrlFlow,
        authDataFlow.filterIsInstance<AccountService.AuthenticationData.LoggedIn>()
    ) { serverUrl, authData ->
        ArtemisContextImpl.LoggedIn(
            serverUrl = serverUrl,
            authToken = authData.authToken,
            loginName = authData.username
        )
    }

    companion object {
        private const val ACCOUNT_DATA_CACHE_NAME = "account_data_cache"
    }

    private val Context.accountDataCache by preferencesDataStore(ACCOUNT_DATA_CACHE_NAME)

    override val accountDataFlow: Flow<Account?> = authDataFlow
        .filterIsInstance<AccountService.AuthenticationData.LoggedIn>()
        .map { authData ->
            val authToken = authData.authToken
            val cacheKey = getAccountDataCacheKey(authToken)
            val cacheEntry = context.accountDataCache.data.first()[cacheKey]
            if (cacheEntry != null) {
                try {
                    val cachedAccount: Account = jsonProvider.applicationJsonConfiguration.decodeFromString(cacheEntry)
                    return@map cachedAccount
                } catch (_: SerializationException) {
                } catch (_: IllegalArgumentException) {
                }
            }
            return@map null
        }

    override suspend fun getAccountData(): NetworkResponse<Account> {
        val serverUrl = serverUrlFlow.first()
        val authToken = authDataFlow.first().authToken

        return performNetworkCall {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments(*Api.Core.Public.path, "account")
                }

                cookieAuth(authToken)
            }.body<Account>()
        }.onSuccess { account ->
            context.accountDataCache.edit { data ->
                data[getAccountDataCacheKey(authToken)] =
                    jsonProvider.applicationJsonConfiguration.encodeToString(account)
            }
        }
    }

    override suspend fun getCachedAccountData(): Account? {
        return accountDataFlow.first()
    }

    private fun getAccountDataCacheKey(bearerToken: String) = stringPreferencesKey(bearerToken)
}
