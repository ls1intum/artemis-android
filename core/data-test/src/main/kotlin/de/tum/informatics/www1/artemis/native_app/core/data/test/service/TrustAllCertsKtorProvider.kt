package de.tum.informatics.www1.artemis.native_app.core.data.test.service

import android.annotation.SuppressLint
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class TrustAllCertsKtorProvider(
    jsonProvider: JsonProvider,
    // Mirrors DefaultTimeoutMillis, which lives in core:core-test and is not visible from here. CI
    // raises DEFAULT_TIMEOUT because the runner is shared and slower than a developer machine, but
    // that only ever reached the test timeouts: the HTTP client stayed at ten seconds, and fixture
    // requests have run past it -- creating a course timed out on develop with
    // "Request timeout has expired [url=.../api/admin/courses, request_timeout=10000 ms]".
    timeoutMillis: Long = System.getenv("DEFAULT_TIMEOUT")?.toLongOrNull() ?: 10_000L
) : KtorProvider {

    private val trustAll = @SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private val httpClient = HttpClient(CIO) {
        // Same as the production client: without it a 4xx is handed to .body() like any
        // other response, and since every model field has a default it decodes into an
        // empty domain object. The E2E suite then sees a successful call that did nothing.
        expectSuccess = true

        install(ContentNegotiation) {
            json(jsonProvider.applicationJsonConfiguration)
        }

        engine {
            https {
                trustManager = trustAll
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = timeoutMillis
            connectTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
        }
    }

    override val ktorClient: HttpClient = httpClient
}