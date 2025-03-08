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
    val groups: List<String> = emptyList()
) : BaseAccount

fun Account.isAtLeastTutorInCourse(course: Course): Boolean {
    return hasGroup(course.instructorGroupName) ||
            hasGroup(course.editorGroupName) ||
            hasGroup(course.teachingAssistantGroupName) ||
            hasAnyAuthorityDirect(listOf(AccountAuthority.ROLE_INSTRUCTOR))
}

private fun Account.hasGroup(groupName: String): Boolean {
    return groupName in groups
}

private fun Account.hasAnyAuthorityDirect(authorities: List<AccountAuthority>): Boolean {
    return this.authorities.any { it in authorities }
}

private fun Account.highestAuthority() = authorities.maxOrNull()