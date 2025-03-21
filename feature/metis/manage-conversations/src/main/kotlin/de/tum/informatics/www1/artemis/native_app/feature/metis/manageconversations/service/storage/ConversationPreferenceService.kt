package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage

import kotlinx.coroutines.flow.Flow

interface ConversationPreferenceService {

    fun getPreferences(serverUrl: String, courseId: Long): Flow<Preferences>

    suspend fun updatePreferences(serverUrl: String, courseId: Long, preferences: Preferences)

    data class Preferences(
        val favouritesExpanded: Boolean,
        val generalsExpanded: Boolean,
        val examsExpanded: Boolean,
        val exercisesExpanded: Boolean,
        val lecturesExpanded: Boolean,
        val groupChatsExpanded: Boolean,
        val personalConversationsExpanded: Boolean,
        val hiddenExpanded: Boolean,
        val savedPostsExpanded: Boolean,
        val recentExpanded: Boolean
    )
}