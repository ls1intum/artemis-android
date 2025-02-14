package de.tum.informatics.www1.artemis.native_app.core.common.artemis_context

data class ArtemisContext(
    val serverUrl: String,
    val authToken: String,
) {
    companion object {
        val Empty = ArtemisContext(
            serverUrl = "",
            authToken = "",
        )
    }
}

