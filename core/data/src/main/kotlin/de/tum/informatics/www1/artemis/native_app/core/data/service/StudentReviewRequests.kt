package de.tum.informatics.www1.artemis.native_app.core.data.service

import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess

class StudentReviewHttpException(val status: Int) : Exception("Student review HTTP $status")

/** A protected read has one captured origin/session and never follows a credential-bearing redirect. */
suspend inline fun <reified T> KtorProvider.studentReviewGet(
    serverUrl: String,
    authToken: String,
    vararg path: String,
    query: Map<String, String> = emptyMap(),
): T {
    val client = ktorClient.config { followRedirects = false; expectSuccess = false }
    return try {
        val response = client.get(serverUrl) {
            cookieAuth(authToken)
            url {
                appendPathSegments(*path)
                query.forEach { (key, value) -> parameters.append(key, value) }
            }
        }
        if (!response.status.isSuccess()) throw StudentReviewHttpException(response.status.value)
        response.body()
    } finally { client.close() }
}
