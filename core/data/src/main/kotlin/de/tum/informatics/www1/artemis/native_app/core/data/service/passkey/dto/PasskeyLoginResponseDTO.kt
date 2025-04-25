package de.tum.informatics.www1.artemis.native_app.core.data.service.passkey.dto

data class PasskeyLoginResponseDTO(
    val redirectUrl: String,
    val authenticated: Boolean
)