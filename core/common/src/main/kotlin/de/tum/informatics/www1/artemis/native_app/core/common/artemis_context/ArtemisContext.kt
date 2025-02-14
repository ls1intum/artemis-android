package de.tum.informatics.www1.artemis.native_app.core.common.artemis_context

data class ArtemisContext(
    val serverUrl: String,
    val authToken: String,
    /** Also referred to as clientId or loginName */
    val username: String,
) {
    companion object {
        val Empty = ArtemisContext(
            serverUrl = "",
            authToken = "",
            username = ""
        )
    }
}

