package de.tum.informatics.www1.artemis.native_app.feature.login.service.oidc

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface OidcAuthService {
    val authCodeFlow: Flow<String>
    fun launchOidcFlow(serverUrl: String, rememberMe: Boolean)
    fun handleRedirectUri(uri: Uri?): Boolean
    fun getAndClearCodeVerifier(): String?
}
