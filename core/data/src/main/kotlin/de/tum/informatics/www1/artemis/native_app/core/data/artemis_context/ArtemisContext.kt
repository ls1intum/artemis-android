package de.tum.informatics.www1.artemis.native_app.core.data.artemis_context

import de.tum.informatics.www1.artemis.native_app.core.model.account.Account


sealed interface ArtemisContext {
    val serverUrl: String


    sealed interface LoggedIn: ArtemisContext {
        val authToken: String
        val loginName: String
    }

    sealed interface AccountLoaded: LoggedIn {
        val account: Account

        val clientId: Long
            get() = account.id
    }

    sealed interface Course: AccountLoaded {
        val courseId: Long
    }
}

