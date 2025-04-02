package de.tum.informatics.www1.artemis.native_app.core.data.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.model.account.Account


object ArtemisContextImpl {

    data object Empty : ArtemisContext {
        override val serverUrl: String = ""
    }

    data class ServerSelected(
        override val serverUrl: String,
    ) : ArtemisContext

    data class LoggedIn(
        override val serverUrl: String,
        override val authToken: String,
        override val loginName: String,
    ) : ArtemisContext.LoggedIn

    data class AccountLoaded(
        override val serverUrl: String,
        override val authToken: String,
        override val loginName: String,
        override val account: Account,
    ): ArtemisContext.AccountLoaded

    data class Course(
        override val serverUrl: String,
        override val authToken: String,
        override val loginName: String,
        override val account: Account,
        override val courseId: Long,
    ) : ArtemisContext.Course
}