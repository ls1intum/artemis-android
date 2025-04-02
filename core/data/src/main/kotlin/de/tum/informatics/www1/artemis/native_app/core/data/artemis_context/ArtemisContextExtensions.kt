package de.tum.informatics.www1.artemis.native_app.core.data.artemis_context

fun ArtemisContext.ifLoggedIn(
    block: (ArtemisContext.LoggedIn) -> Unit
) {
    if (this is ArtemisContext.LoggedIn) {
        block(this)
    }
}

val ArtemisContext.authTokenOrEmptyString: String
    get() = if (this is ArtemisContext.LoggedIn) {
        authToken
    } else {
        ""
    }