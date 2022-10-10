package de.tum.informatics.www1.artemis.native_app.android.content

/**
 * TODO: Why are some properties nullable?
 *
 * From: https://github.com/ls1intum/Artemis/blob/6edbff8a7fac2748e130d9c26f260eb6ad5e81e7/src/main/webapp/app/shared/layouts/profiles/profile-info.model.ts
 */
@kotlinx.serialization.Serializable
data class ProfileInfo(
    val activeProfiles: List<String> = emptyList(),
    val inProduction: Boolean,
    val contact: String,
    val testServer: Boolean?,
    val registrationEnabled: Boolean?,
    val needsToAcceptTerms: Boolean?,
    val allowedEmailPattern: String?,
    val allowedEmailPatternReadable: String?,
    val allowedLdapUsernamePattern: String?,
    val allowedCourseRegistrationUsernamePattern: String?,
    val accountName: String?,
    val externalCredentialProvider: String,
    val externalPasswordResetLinkMap: Map<String, String>
)