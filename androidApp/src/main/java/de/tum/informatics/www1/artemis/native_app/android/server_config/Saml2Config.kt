package de.tum.informatics.www1.artemis.native_app.android.server_config

import kotlinx.serialization.Serializable

/**
 * From: https://github.com/ls1intum/Artemis/blob/develop/src/main/webapp/app/home/saml2-login/saml2.config.ts
 */
@Serializable
data class Saml2Config(
    val identityProviderName: String? = null,
    val buttonLabel: String? = null,
    val passwordLoginDisabled: Boolean = false,
    val enablePassword: Boolean = false
)