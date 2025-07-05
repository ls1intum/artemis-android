package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.model.ConversationsOverviewSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ConversationPreferenceStorageServiceImpl(private val context: Context) :
    ConversationPreferenceService {

    private val Context.dataStore by preferencesDataStore("conversation_preferences")

    override fun getPreferences(
        serverUrl: String,
        courseId: Long
    ): Flow<ConversationPreferenceService.Preferences> = context.dataStore.data.map { data ->
        ConversationPreferenceService.Preferences(
            expandedStateBySection = ConversationsOverviewSection.entries.associateWith { section ->
                data[getKey(serverUrl, courseId, section.name)] ?: section.expandedByDefault
            }
        )
    }

    override suspend fun updatePreferences(serverUrl: String, courseId: Long, preferences: ConversationPreferenceService.Preferences) {
        context.dataStore.edit { data ->
            preferences.expandedStateBySection.forEach { (section, expanded) ->
                data[getKey(serverUrl, courseId, section.name)] = expanded
            }
        }
    }

    private fun getKey(serverUrl: String, courseId: Long, key: String): Preferences.Key<Boolean> =
        booleanPreferencesKey("$serverUrl|$courseId|$key")
}
