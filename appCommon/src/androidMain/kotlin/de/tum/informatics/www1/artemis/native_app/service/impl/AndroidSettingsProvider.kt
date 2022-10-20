package de.tum.informatics.www1.artemis.native_app.service.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.datastore.DataStoreSettings
import de.tum.informatics.www1.artemis.native_app.service.AccountService
import de.tum.informatics.www1.artemis.native_app.service.SettingsProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.properties.ReadOnlyProperty

class AndroidSettingsProvider(
    private val context: Context,
) : SettingsProvider {

    override fun createSettings(name: String): FlowSettings {
        val dataStore = PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile(name)
        }

        return DataStoreSettings(dataStore)
    }
}