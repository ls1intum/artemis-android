package de.tum.informatics.www1.artemis.native_app.feature.metis.service

import kotlinx.coroutines.flow.Flow

internal interface ConversationPreferenceService {

    fun getPreferences(serverUrl: String, courseId: Long): Flow<Preferences>

    suspend fun updatePreferences(serverUrl: String, courseId: Long, preferences: Preferences)

    data class Preferences(
        val favouritesExpanded: Boolean,
        val channelsExpanded: Boolean,
        val groupChatsExpanded: Boolean,
        val personalConversationsExpanded: Boolean,
        val hiddenExpanded: Boolean
    )
}