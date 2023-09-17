package de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation

import de.tum.informatics.www1.artemis.native_app.core.model.account.BaseAccount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationUser(
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
    val isChannelModerator: Boolean = false,
    val isRequestingUser: Boolean = false,
    val isInstructor: Boolean = false,
    val isEditor: Boolean = false,
    val isTeachingAssistant: Boolean = false,
    val isStudent: Boolean = false
) : BaseAccount