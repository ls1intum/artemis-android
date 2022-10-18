package de.tum.informatics.www1.artemis.native_app.android.content.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val activated: Boolean = false,
    val authorities: List<String> = emptyList(),
    @SerialName("login")
    val username: String?,
    val email: String?,
    val name: String? = "",
    val internal: Boolean = true,
    val firstName: String = "",
    val lastName: String = "",
    val langKey: String = "en",
    val imageUrl: String?,
    val id: Int?
)