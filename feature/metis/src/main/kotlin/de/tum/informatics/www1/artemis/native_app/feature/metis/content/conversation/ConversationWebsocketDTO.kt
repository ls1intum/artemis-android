package de.tum.informatics.www1.artemis.native_app.feature.metis.content.conversation

import de.tum.informatics.www1.artemis.native_app.feature.metis.content.Conversation
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisPostAction
import kotlinx.serialization.Serializable

@Serializable
data class ConversationWebsocketDTO(
    val conversation: Conversation,
    val crudAction: MetisPostAction
)
