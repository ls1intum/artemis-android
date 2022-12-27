package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The context in which metis is used, e.g. in a course, an exercise or personal communication.
 */
@Serializable
sealed class MetisContext(
    val postPathSegments: List<String>,
    val standalonePostResourceEndpoint: String,
    val answerPostResourceEndpoint: String
) {
    abstract val courseId: Long

    @Serializable
    @SerialName("course")
    data class Course(override val courseId: Long) :
        MetisContext(listOf("courses", courseId.toString(), "discussion"), "posts", "answer-posts")

    @Serializable
    @SerialName("exercise")
    data class Exercise(override val courseId: Long, val exerciseId: Long) :
        MetisContext(
            listOf("courses", courseId.toString(), "exercises", exerciseId.toString()),
            "posts",
            "answer-posts"
        )

    @Serializable
    @SerialName("lecture")
    data class Lecture(override val courseId: Long, val lectureId: Long) :
        MetisContext(
            listOf("courses", courseId.toString(), "lectures", lectureId.toString()),
            "posts",
            "answer-posts"
        )

    @Serializable
    @SerialName("conversation")
    data class Conversation(override val courseId: Long, val conversationId: Int) :
        MetisContext(listOf(), "messages", "answer-messages")
}
