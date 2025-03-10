package de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks

object CommunicationDeeplinks {

    object ToConversation : ArtemisDeeplink() {
        override val path = "courses/{courseId}/messages?conversationId={conversationId}"
        override val type = Type.IN_APP_AND_WEB

        fun inAppLink(courseId: Long, conversationId: Long): String {
            return "$IN_APP_HOST/courses/$courseId/messages?conversationId=$conversationId"
        }
    }

    object ToOneToOneChatByUsername : ArtemisDeeplink() {
        override val path = "courses/{courseId}/messages?username={username}"
        override val type = Type.IN_APP_AND_WEB

        fun inAppLink(courseId: Long, username: String): String {
            return "$IN_APP_HOST/courses/$courseId/messages?username=$username"
        }
    }

    object ToOneToOneChatByUserId : ArtemisDeeplink() {
        override val path = "courses/{courseId}/messages?userId={userId}"
        override val type = Type.IN_APP_AND_WEB

        fun inAppLink(courseId: Long, userId: Long): String {
            return "$IN_APP_HOST/courses/$courseId/messages?userId=$userId"
        }
    }

    object ToPostById: ArtemisDeeplink() {
        override val path = "courses/{courseId}/{conversationId}/{postId}"
        override val type = Type.ONLY_IN_APP

        fun inAppLink(courseId: Long, conversationId: Long, postId: Long): String {
            return "$IN_APP_HOST/courses/$courseId/$conversationId/$postId"
        }
    }

    object ToConversationCourseAgnostic : ArtemisDeeplink() {
        override val path = "messages?conversationId={conversationId}"
        override val type = Type.ONLY_IN_APP

        fun inAppLink(conversationId: Long): String {
            return "$IN_APP_HOST/messages?conversationId=$conversationId"
        }
    }
}