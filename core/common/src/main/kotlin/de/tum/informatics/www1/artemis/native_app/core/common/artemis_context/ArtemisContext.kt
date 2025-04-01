package de.tum.informatics.www1.artemis.native_app.core.common.artemis_context

sealed interface ArtemisContext {
    val serverUrl: String

    sealed interface LoggedIn: ArtemisContext {
        val authToken: String
        /** Also referred to as loginName */
        val username: String
    }

    sealed interface Course: LoggedIn {
        val courseId: Long
    }
}

