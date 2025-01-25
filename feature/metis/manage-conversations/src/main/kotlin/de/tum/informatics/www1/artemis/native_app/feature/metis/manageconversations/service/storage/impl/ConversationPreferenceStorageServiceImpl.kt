package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.impl

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage.ConversationPreferenceService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ConversationPreferenceStorageServiceImpl(private val context: Context) :
    ConversationPreferenceService {

    private companion object {
        private const val KEY_FAVOURITES_EXPANDED = "fav"
        private const val KEY_CHANNELS_EXPANDED = "channels"
        private const val KEY_GROUP_CHATS_EXPANDED = "group_chats"
        private const val KEY_PERSONAL_CONVERSATIONS_EXPANDED = "personal_conv"
        private const val KEY_HIDDEN_EXPANDED = "hidden"
        private const val KEY_EXAMS_EXPANDED = "exams"
        private const val KEY_EXERCISES_EXPANDED = "exercises"
        private const val KEY_LECTURES_EXPANDED = "lectures"
        private const val KEY_SAVED_POSTS_EXPANDED = "saved_posts"
    }

    private val Context.dataStore by preferencesDataStore("conversation_preferences")

    override fun getPreferences(
        serverUrl: String,
        courseId: Long
    ): Flow<ConversationPreferenceService.Preferences> = context.dataStore.data.map { data ->
        ConversationPreferenceService.Preferences(
            favouritesExpanded = data[getKey(serverUrl, courseId, KEY_FAVOURITES_EXPANDED)] ?: true,
            generalsExpanded = data[getKey(serverUrl, courseId, KEY_CHANNELS_EXPANDED)] ?: true,
            groupChatsExpanded = data[getKey(serverUrl, courseId, KEY_GROUP_CHATS_EXPANDED)] ?: true,
            personalConversationsExpanded = data[getKey(serverUrl, courseId, KEY_PERSONAL_CONVERSATIONS_EXPANDED)] ?: true,
            hiddenExpanded = data[getKey(serverUrl, courseId, KEY_HIDDEN_EXPANDED)] ?: false,
            examsExpanded = data[getKey(serverUrl, courseId, KEY_EXAMS_EXPANDED)] ?: true,
            exercisesExpanded = data[getKey(serverUrl, courseId, KEY_EXERCISES_EXPANDED)] ?: true,
            lecturesExpanded = data[getKey(serverUrl, courseId, KEY_LECTURES_EXPANDED)] ?: true,
            savedPostsExpanded = data[getKey(serverUrl, courseId, KEY_SAVED_POSTS_EXPANDED)] ?: false
        )
    }

    override suspend fun updatePreferences(serverUrl: String, courseId: Long, preferences: ConversationPreferenceService.Preferences) {
        context.dataStore.edit { data ->
            data[getKey(serverUrl, courseId, KEY_FAVOURITES_EXPANDED)] = preferences.favouritesExpanded
            data[getKey(serverUrl, courseId, KEY_CHANNELS_EXPANDED)] = preferences.generalsExpanded
            data[getKey(serverUrl, courseId, KEY_GROUP_CHATS_EXPANDED)] = preferences.groupChatsExpanded
            data[getKey(serverUrl, courseId, KEY_PERSONAL_CONVERSATIONS_EXPANDED)] = preferences.personalConversationsExpanded
            data[getKey(serverUrl, courseId, KEY_HIDDEN_EXPANDED)] = preferences.hiddenExpanded
            data[getKey(serverUrl, courseId, KEY_EXAMS_EXPANDED)] = preferences.examsExpanded
            data[getKey(serverUrl, courseId, KEY_EXERCISES_EXPANDED)] = preferences.exercisesExpanded
            data[getKey(serverUrl, courseId, KEY_LECTURES_EXPANDED)] = preferences.lecturesExpanded
            data[getKey(serverUrl, courseId, KEY_SAVED_POSTS_EXPANDED)] = preferences.savedPostsExpanded
        }
    }

    private fun getKey(serverUrl: String, courseId: Long, key: String): Preferences.Key<Boolean> =
        booleanPreferencesKey("$serverUrl|$courseId|$key")
}
