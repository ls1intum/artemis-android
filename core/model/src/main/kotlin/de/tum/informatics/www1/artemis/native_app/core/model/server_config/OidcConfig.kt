package de.tum.informatics.www1.artemis.native_app.core.model.server_config

import kotlinx.serialization.Serializable

@Serializable
data class OidcConfig(
    val buttonLabel: String? = null,
    val clientName: String? = null
)
