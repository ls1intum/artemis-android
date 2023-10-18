package de.tum.informatics.www1.artemis.native_app.feature.metis.service.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.metis.service.CodeOfConductStorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CodeOfConductStorageServiceImpl(private val context: Context) : CodeOfConductStorageService {

    companion object {
        private const val DATA_STORE_NAME = "coc_local_storage"
    }

    private val Context.dataStore by preferencesDataStore(DATA_STORE_NAME)

    override suspend fun isCodeOfConductAccepted(
        serverHost: String,
        courseId: Long,
        codeOfConduct: String
    ): Flow<Boolean> {
        return context.dataStore.data.map { data ->
            data[getCocKey(serverHost, courseId)] == codeOfConduct.hashCode()
        }
    }

    override suspend fun acceptCodeOfConduct(
        serverHost: String,
        courseId: Long,
        codeOfConduct: String
    ) {
        context.dataStore.edit { data ->
            data[getCocKey(serverHost, courseId)] = codeOfConduct.hashCode()
        }
    }

    private fun getCocKey(serverHost: String, courseId: Long): Preferences.Key<Int> =
        intPreferencesKey("$serverHost|$courseId")
}
