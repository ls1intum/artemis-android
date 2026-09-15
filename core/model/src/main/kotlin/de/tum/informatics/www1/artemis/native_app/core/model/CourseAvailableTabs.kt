package de.tum.informatics.www1.artemis.native_app.core.model

import kotlinx.serialization.Serializable

/**
 * Which tabs a course offers the requesting user, as answered by
 * "api/course/courses/{courseId}/available-tabs".
 *
 * This is the server's single source of truth for tab availability since Artemis 10; before that,
 * flags such as "faqEnabled" were carried on the course itself.
 */
@Serializable
data class CourseAvailableTabs(
    val lectures: Boolean = false,
    val exams: Boolean = false,
    val competencies: Boolean = false,
    val tutorialGroups: Boolean = false,
    val iris: Boolean = false,
    val faq: Boolean = false,
    val learningPaths: Boolean = false,
    val communication: Boolean = false,
    val training: Boolean = false,
)
