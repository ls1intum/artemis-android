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

class TrustAllCertsKtorProvider(jsonProvider: JsonProvider, timeoutMillis: Long = 10000) : KtorProvider {

    private val trustAll = @SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private val httpClient = HttpClient(CIO) {
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