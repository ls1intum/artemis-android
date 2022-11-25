package de.tum.informatics.www1.artemis.native_app.core.communication

/**
 * The context in which metis is used, e.g. in a course, an exercise or personal communication.
 */
sealed class MetisContext(val courseId: Int, val postPathSegments: List<String>, val resourceEndpoint: String) {

    class Course(courseId: Int) :
        MetisContext(courseId, listOf("courses", courseId.toString(), "discussion"), "posts")

    class Exercise(courseId: Int, val exerciseId: Int) :
        MetisContext(
            courseId,
            listOf("courses", courseId.toString(), "exercises", exerciseId.toString()),
            "posts"
        )

    class Lecture(courseId: Int, val lectureId: Int) :
        MetisContext(
            courseId,
            listOf("courses", courseId.toString(), "lectures", lectureId.toString()),
            "posts"
        )

    class Conversation(courseId: Int, val conversationId: Int): MetisContext(courseId, listOf(), "messages")
}
