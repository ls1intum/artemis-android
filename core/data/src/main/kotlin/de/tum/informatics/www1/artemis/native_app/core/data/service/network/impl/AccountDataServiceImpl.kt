package de.tum.informatics.www1.artemis.native_app.core.data.service.network.impl

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.core.data.NetworkResponse
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.data.onSuccess
import de.tum.informatics.www1.artemis.native_app.core.data.performNetworkCall
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.AccountDataService
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.model.account.Account
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

internal class AccountDataServiceImpl(
    private val context: Context,
    private val ktorProvider: KtorProvider,
    private val jsonProvider: JsonProvider
) : AccountDataService {

    companion object {
        private const val ACCOUNT_DATA_CACHE_NAME = "account_data_cache"
    }

    private val Context.accountDataCache by preferencesDataStore(ACCOUNT_DATA_CACHE_NAME)

    override suspend fun getAccountData(
        serverUrl: String,
        bearerToken: String
    ): NetworkResponse<Account> {
        return performNetworkCall<Account> {
            ktorProvider.ktorClient.get(serverUrl) {
                url {
                    appendPathSegments("api", "public", "account")
                }

                contentType(ContentType.Application.Json)
                cookieAuth(bearerToken)
            }.body()
        }.onSuccess { account ->
            context.accountDataCache.edit { data ->
                data[getAccountDataCacheKey(bearerToken)] = jsonProvider.applicationJsonConfiguration.encodeToString(account)
            }
        }
    }

    override suspend fun getCachedAccountData(serverUrl: String, bearerToken: String): Account? {
        val cacheData = context.accountDataCache.data.first()
        val cacheKey = getAccountDataCacheKey(bearerToken)
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
