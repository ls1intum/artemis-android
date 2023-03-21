package de.tum.informatics.www1.artemis.native_app.core.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class User(
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
    val password: String = ""
) : BaseAccount