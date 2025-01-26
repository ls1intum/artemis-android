package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto

import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.ICourseUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseUser(
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
    override val isInstructor: Boolean = false,
    override val isEditor: Boolean = false,
    override val isTeachingAssistant: Boolean = false,
    override val isStudent: Boolean = false
) : ICourseUser