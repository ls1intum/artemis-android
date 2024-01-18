package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.service

interface CreatePostService {

    fun createPost(courseId: Long, conversationId: Long, content: String)

    fun createAnswerPost(courseId: Long, conversationId: Long, parentPostId: Long, content: String)
}