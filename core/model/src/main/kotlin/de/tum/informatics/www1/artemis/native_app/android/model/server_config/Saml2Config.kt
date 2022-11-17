package de.tum.informatics.www1.artemis.native_app.android.model.server_config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * From: https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/home/saml2-login/saml2.config.ts
 */
@Serializable
data class Saml2Config(
    val identityProviderName: String? = null,
    @SerialName("button-label")
    val buttonLabel: String? = null,
    val passwordLoginDisabled: Boolean = false,
    val enablePassword: Boolean = false
)