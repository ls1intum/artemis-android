package de.tum.informatics.www1.artemis.native_app.feature.settings.service.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PasskeyDTO(
    /** The credential ID of the passkey, encoded in Base64url format. */
    val credentialId: String,
    val label: String,
    /** ISO 8601 date string */
    val created: Instant,
    /** ISO 8601 date string */
    val lastUsed: Instant
)
