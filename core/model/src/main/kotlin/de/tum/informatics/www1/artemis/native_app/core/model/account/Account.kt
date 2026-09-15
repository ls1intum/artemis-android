package de.tum.informatics.www1.artemis.native_app.core.model.account

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class Account(
    override val activated: Boolean = false,
    override val authorities: List<AccountAuthority> = emptyList(),
    @SerialName("login")
    override val username: String? = null,
    override val email: String? = null,
    override val name: String? = "",
    override val internal: Boolean = true,
    override val firstName: String = "",
    override val lastName: String = "",
    override val langKey: String = "en",
    override val imageUrl: String? = null,
    override val id: Long = 0L,
    /**
     * The roles this account holds per course. Artemis 10 replaced the course group names the client
     * used to match against the account's groups with this list.
     */
    val courseRoles: List<CourseAccessRights> = emptyList()
) : BaseAccount {

    val hasCustomProfilePicture: Boolean
        get() = imageUrl != null

    fun isAtLeastTutorInCourse(course: Course): Boolean =
        hasCourseRoleAtLeast(course.id, CourseRole.TEACHING_ASSISTANT)

    private fun hasCourseRoleAtLeast(courseId: Long?, minimum: CourseRole): Boolean {
        if (hasAnyAuthorityDirect(listOf(AccountAuthority.ROLE_ADMIN))) return true
        if (courseId == null) return false

        return courseRoles
            .firstOrNull { it.courseId == courseId }
            ?.roles
            .orEmpty()
            .any { it.isAtLeast(minimum) }
    }

    private fun hasAnyAuthorityDirect(authorities: List<AccountAuthority>): Boolean {
        return this.authorities.any { it in authorities }
    }
}
