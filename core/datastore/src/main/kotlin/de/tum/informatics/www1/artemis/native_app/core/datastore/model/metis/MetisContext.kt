package de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis

/**
 * The context in which metis is used, e.g. in a course, an exercise or personal communication.
 */
sealed class MetisContext(
    val postPathSegments: List<String>,
    val standalonePostResourceEndpoint: String,
    val answerPostResourceEndpoint: String
) {
    abstract val courseId: Int

    data class Course(override val courseId: Int) :
        MetisContext(listOf("courses", courseId.toString(), "discussion"), "posts", "answer-posts")

    data class Exercise(override val courseId: Int, val exerciseId: Int) :
        MetisContext(
            listOf("courses", courseId.toString(), "exercises", exerciseId.toString()),
            "posts",
            "answer-posts"
        )

    data class Lecture(override val courseId: Int, val lectureId: Int) :
        MetisContext(
            listOf("courses", courseId.toString(), "lectures", lectureId.toString()),
            "posts",
            "answer-posts"
        )

    data class Conversation(override val courseId: Int, val conversationId: Int) :
        MetisContext(listOf(), "messages", "answer-messages")
}
