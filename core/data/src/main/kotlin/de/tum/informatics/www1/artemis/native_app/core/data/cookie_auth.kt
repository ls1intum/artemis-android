package de.tum.informatics.www1.artemis.native_app.core.data

import io.ktor.client.request.*
import io.ktor.http.*

fun HttpMessageBuilder.cookieAuth(token: String): Unit =
    header(HttpHeaders.Cookie, "jwt=$token")