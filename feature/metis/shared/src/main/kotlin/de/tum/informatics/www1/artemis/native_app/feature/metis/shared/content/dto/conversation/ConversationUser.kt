package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation

import de.tum.informatics.www1.artemis.native_app.core.model.account.AccountAuthority
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.ICourseUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationUser(
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
    val isChannelModerator: Boolean = false,
    val isRequestingUser: Boolean = false,
    override val isInstructor: Boolean = false,
    override val isEditor: Boolean = false,
    override val isTeachingAssistant: Boolean = false,
    override val isStudent: Boolean = false
) : ICourseUser