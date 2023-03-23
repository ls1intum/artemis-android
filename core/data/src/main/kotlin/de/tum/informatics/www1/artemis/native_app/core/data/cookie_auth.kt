package de.tum.informatics.www1.artemis.native_app.core.data

import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMessageBuilder

fun HttpMessageBuilder.cookieAuth(token: String): Unit =
    header(HttpHeaders.Cookie, "jwt=$token")