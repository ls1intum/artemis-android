package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.conversation.settings

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser

data class PerformActionOnUserData(
    val user: ConversationUser,
    val userAction: UserAction
)