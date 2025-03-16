package de.tum.informatics.www1.artemis.native_app.core.model.account

import kotlinx.serialization.SerialName

interface BaseAccount {
    val activated: Boolean
    val authorities: List<AccountAuthority>
    @SerialName("login")
    val username: String?
    val email: String?
    val name: String?
    val internal: Boolean
    val firstName: String
    val lastName: String
    val langKey: String
    val imageUrl: String?
    val id: Long
}