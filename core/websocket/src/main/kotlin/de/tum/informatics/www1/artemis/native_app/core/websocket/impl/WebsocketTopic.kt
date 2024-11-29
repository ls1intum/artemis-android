package de.tum.informatics.www1.artemis.native_app.core.websocket.impl

object WebsocketTopic {

    /**
     * Returns the topic for conversation updates for a course-wide conversation.
     */
    fun getCourseWideConversationUpdateTopic(courseId: Long): String {
        return "/topic/metis/courses/$courseId"
    }

    /**
     * Returns the topic for conversation updates for a non-course-wide conversation.
     */
    fun getNormalConversationUpdateTopic(userId: Long): String {
        return "/topic/user/$userId/notifications/conversations"
    }

    /**
     * Returns the topic for conversation meta updates. This includes channel creation, deletion,
     * and updates (like changing the channel name).
     */
    fun getConversationMetaUpdateTopic(courseId: Long, userId: Long): String {
        return "/user/topic/metis/courses/$courseId/conversations/user/$userId"
    }
}