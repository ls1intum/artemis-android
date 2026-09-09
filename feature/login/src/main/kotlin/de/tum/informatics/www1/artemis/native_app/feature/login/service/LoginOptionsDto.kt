package de.tum.informatics.www1.artemis.native_app.feature.login.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginOptionsDto(
    val loginMethod: LoginMethod,
    val idpName: String? = null
)

@Serializable
enum class LoginMethod {
    @SerialName("PASSWORD") PASSWORD,
    @SerialName("SAML2") SAML2,
    @SerialName("OIDC") OIDC
}