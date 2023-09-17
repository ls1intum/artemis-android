package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.dto

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.content.MetisPostAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationWebsocketDto(
    val conversation: Conversation,
    @SerialName("metisCrudAction")
    val crudAction: MetisPostAction
)
