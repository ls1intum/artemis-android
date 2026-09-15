package de.tum.informatics.www1.artemis.native_app.core.model.account

import kotlinx.serialization.Serializable

/**
 * The roles the account holds in one course.
 *
 * Artemis 10 replaced the course group names the client used to compare against the account's groups
 * with this per-course role list, which the account endpoint returns directly.
 */
@Serializable
data class CourseAccessRights(
    val courseId: Long = 0L,
    val roles: Set<CourseRole> = emptySet(),
)

/**
 * Ordered from least to most privileged, matching the server. [isAtLeast] relies on that order.
 */
@Serializable
enum class CourseRole {
    STUDENT,
    TEACHING_ASSISTANT,
    EDITOR,
    INSTRUCTOR;

    fun isAtLeast(minimum: CourseRole): Boolean = ordinal >= minimum.ordinal
}
