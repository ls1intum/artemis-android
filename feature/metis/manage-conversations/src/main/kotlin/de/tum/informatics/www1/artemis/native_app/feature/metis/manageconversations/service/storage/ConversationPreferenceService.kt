package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.service.storage

import de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.overview.ConversationsOverviewSection
import kotlinx.coroutines.flow.Flow

interface ConversationPreferenceService {

    fun getPreferences(serverUrl: String, courseId: Long): Flow<Preferences>

    suspend fun updatePreferences(serverUrl: String, courseId: Long, preferences: Preferences)

    data class Preferences(
        val expandedStateBySection: Map<ConversationsOverviewSection, Boolean>
    ) {
        fun toggle(section: ConversationsOverviewSection): Preferences {
            val currentState = expandedStateBySection[section] ?: section.expandedByDefault
            return copy(expandedStateBySection = expandedStateBySection + (section to !currentState))
        }

        fun isExpanded(section: ConversationsOverviewSection): Boolean {
            return expandedStateBySection[section] ?: section.expandedByDefault
        }
    }
}