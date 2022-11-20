package de.tum.informatics.www1.artemis.native_app.core.model.server_config

/**
 * TODO: Why are some properties nullable?
 *
 * From: https://github.com/ls1intum/Artemis/blob/6edbff8a7fac2748e130d9c26f260eb6ad5e81e7/src/main/webapp/app/shared/layouts/profiles/profile-info.model.ts
 */
@kotlinx.serialization.Serializable
data class ProfileInfo(
    val activeProfiles: List<String> = emptyList(),
    val inProduction: Boolean = false,
    val contact: String = "",
    val testServer: Boolean? = null,
    val registrationEnabled: Boolean? = null,
    val needsToAcceptTerms: Boolean = false,
    val allowedEmailPattern: String? = null,
    val allowedEmailPatternReadable: String? = null,
    val allowedLdapUsernamePattern: String? = null,
    val allowedCourseRegistrationUsernamePattern: String? = null,
    val accountName: String? = null,
    val externalCredentialProvider: String = "",
    val externalPasswordResetLinkMap: Map<String, String> = emptyMap(),
    /**
     * Set if the server allows a saml2 based login. If not set, it is also not supported.
     */
    val saml2: Saml2Config? = null
) {
    /**
     * If a login using a username-password combination is not possible.
     */
    val isPasswordLoginDisabled: Boolean = saml2 != null && (saml2.passwordLoginDisabled || !saml2.enablePassword)
}