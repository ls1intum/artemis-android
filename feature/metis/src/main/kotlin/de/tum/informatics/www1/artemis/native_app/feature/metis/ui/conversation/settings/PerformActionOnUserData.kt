package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.ConversationUser

data class PerformActionOnUserData(
    val user: ConversationUser,
    val userAction: UserAction
)