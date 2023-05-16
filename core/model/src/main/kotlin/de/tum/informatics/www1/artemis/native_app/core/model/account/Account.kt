package de.tum.informatics.www1.artemis.native_app.core.model.account

import de.tum.informatics.www1.artemis.native_app.core.model.Course
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class Account(
    override val activated: Boolean = false,
    override val authorities: List<String> = emptyList(),
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
    val groups: List<String> = emptyList()
) : BaseAccount

private const val AuthorityAdmin = "ROLE_ADMIN"

fun Account.isAtLeastTutorInCourse(course: Course): Boolean {
    return hasGroup(course.instructorGroupName) ||
            hasGroup(course.editorGroupName) ||
            hasGroup(course.teachingAssistantGroupName) ||
            hasAnyAuthorityDirect(listOf(AuthorityAdmin))
}

private fun Account.hasGroup(groupName: String): Boolean {
    return groupName in groups
}

private fun Account.hasAnyAuthorityDirect(authorities: List<String>): Boolean {
    return this.authorities.any { it in authorities }
}