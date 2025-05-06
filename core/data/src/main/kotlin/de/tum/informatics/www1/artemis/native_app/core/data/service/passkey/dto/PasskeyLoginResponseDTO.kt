package de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyLoginResponseDTO(
    val redirectUrl: String,
    val authenticated: Boolean
)