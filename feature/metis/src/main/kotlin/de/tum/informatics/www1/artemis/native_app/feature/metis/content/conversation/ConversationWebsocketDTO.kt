package de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.dto.conversation.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisPostAction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationWebsocketDTO(
    val conversation: Conversation,
    @SerialName("metisCrudAction")
    val crudAction: MetisPostAction
)
