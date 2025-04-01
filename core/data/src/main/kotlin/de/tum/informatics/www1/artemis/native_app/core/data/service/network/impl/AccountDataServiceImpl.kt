package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.service.Api
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.artemis_context.LoggedInBasedServiceImpl
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException

internal class AccountDataServiceImpl(
    private val context: Context,
    ktorProvider: KtorProvider,
    artemisContextProvider: ArtemisContextProvider,
    private val jsonProvider: JsonProvider,
) : AccountDataService, LoggedInBasedServiceImpl(ktorProvider, artemisContextProvider) {

    companion object {
        private const val ACCOUNT_DATA_CACHE_NAME = "account_data_cache"
    }

    private val Context.accountDataCache by preferencesDataStore(ACCOUNT_DATA_CACHE_NAME)

    override suspend fun getAccountData(): NetworkResponse<Account> {
        return getRequest<Account> {
            url {
                appendPathSegments(*Api.Core.Public.path, "account")
            }
        }.onSuccess { account ->
            context.accountDataCache.edit { data ->
                data[getAccountDataCacheKey(authToken())] = jsonProvider.applicationJsonConfiguration.encodeToString(account)
            }
        }
    }

    override suspend fun getCachedAccountData(): Account? {
        val cacheData = context.accountDataCache.data.first()
        val cacheKey = getAccountDataCacheKey(authToken())
        val cacheEntry = cacheData[cacheKey]
        if (cacheEntry != null) {
            try {
                val cachedAccount: Account = jsonProvider.applicationJsonConfiguration.decodeFromString(cacheEntry)
                return cachedAccount
            } catch (_: SerializationException) {
            } catch (_: IllegalArgumentException) {
            }
        }

        return null
    }

    private fun getAccountDataCacheKey(bearerToken: String) = stringPreferencesKey(bearerToken)
}
