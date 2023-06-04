package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CommunicationPostTarget(
    @SerialName("message")
    val message: String,
    @SerialName("entity")
    val entity: String,
    @SerialName("id")
    override val postId: Long,
    @SerialName("course")
    val courseId: Long,
    @SerialName("conversation")
    val conversationId: Long
) : MetisTarget {

    companion object {
        const val MESSAGE_NEW_MESSAGE = "new-message"
        const val MESSAGE_NEW_REPLY = "new-reply"
    }

    override val metisContext: MetisContext = MetisContext.Conversation(courseId, conversationId)
}