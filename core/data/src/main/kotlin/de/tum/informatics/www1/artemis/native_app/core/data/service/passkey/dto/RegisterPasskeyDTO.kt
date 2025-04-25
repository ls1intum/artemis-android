package de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto

import androidx.credentials.Credential

data class RegisterPasskeyDTO(
    val publicKey: PublicKeyDTO
)

data class PublicKeyDTO(
    /** should always be defined, otherwise the server won't accept the passkey registration */
    val credential: Credential?,
    val label: String
)