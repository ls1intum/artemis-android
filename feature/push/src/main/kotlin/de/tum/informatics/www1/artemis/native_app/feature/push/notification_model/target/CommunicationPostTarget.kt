package de.tum.informatics.www1.artemis.native_app.feature.push.notification_model.target

import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CommunicationPostTarget(
    @SerialName("id")
    override val postId: Long,
    @SerialName("course")
    val courseId: Long,
    @SerialName("conversation")
    val conversationId: Long
) : MetisTarget {
    override val metisContext: MetisContext = MetisContext.Conversation(courseId, conversationId)
}