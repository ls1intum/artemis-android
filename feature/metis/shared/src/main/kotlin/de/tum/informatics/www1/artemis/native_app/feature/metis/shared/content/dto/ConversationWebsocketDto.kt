package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisCrudAction
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import kotlinx.serialization.Serializable

@Serializable
data class ConversationWebsocketDto(
    val conversation: Conversation,
    val action: MetisCrudAction
)
