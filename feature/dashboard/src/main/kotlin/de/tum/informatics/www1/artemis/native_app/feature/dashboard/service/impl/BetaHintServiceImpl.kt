package de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.impl

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.BuildConfig
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.service.BetaHintService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BetaHintServiceImpl(private val context: Context) : BetaHintService {

    private companion object {
        private const val DATA_STORE_KEY = "beta_hint_store"

        private val KEY_DISMISSED = booleanPreferencesKey("dismissed")
    }

    private val Context.storage by preferencesDataStore(DATA_STORE_KEY)

    private val isBetaHintDismissed: Flow<Boolean> = context.storage.data.map { it[KEY_DISMISSED] ?: false }

    override val shouldShowBetaHint: Flow<Boolean> = isBetaHintDismissed.map { dismissed ->
        BuildConfig.isBeta && !dismissed
    }

    override suspend fun dismissBetaHintPermanently() {
        context.storage.edit { data ->
            data[KEY_DISMISSED] = true
        }
    }
}
