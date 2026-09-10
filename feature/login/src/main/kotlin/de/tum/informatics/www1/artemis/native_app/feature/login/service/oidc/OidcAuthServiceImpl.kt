package de.tum.informatics.www1.artemis.native_app.feature.login.service.oidc

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class OidcAuthServiceImpl (
    private val context: Context
): OidcAuthService {
    private var codeVerifier: String? = null
    private val _authCodeChannel = Channel<String>(Channel.BUFFERED)
    override val authCodeFlow: Flow<String> = _authCodeChannel.receiveAsFlow()

    override fun launchOidcFlow(serverUrl: String, rememberMe: Boolean) {
        val verifier = PKCEService.generateCodeVerifier()
        val codeChallenge = PKCEService.generateCodeChallenge(verifier)
        this.codeVerifier = verifier
        val authUrl = URLBuilder(serverUrl).apply {
            appendPathSegments("oauth2", "authorization", "oidc")
            parameters.append("redirect", "ios")
            parameters.append("code_challenge", codeChallenge)
            parameters.append("rememberMe", rememberMe.toString())
        }.buildString()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, authUrl.toUri())
    }

    override fun handleRedirectUri(uri: Uri?): Boolean {
        if (uri != null && uri.scheme == "de.tum.cit.ase.artemis" && uri.host == "oauth2callback") {
            val code = uri.getQueryParameter("code")
            if (!code.isNullOrBlank()) {
                _authCodeChannel.trySend(code)
                return true
            }
        }
        return false
    }

    override fun getAndClearCodeVerifier(): String? {
        val verifier = codeVerifier
        codeVerifier = null
        return verifier
    }
}