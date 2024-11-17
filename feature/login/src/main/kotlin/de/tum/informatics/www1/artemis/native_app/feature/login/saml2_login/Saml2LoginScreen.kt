package de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login

import android.annotation.SuppressLint
import android.webkit.CookieManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.accompanist.AccompanistWebViewClient
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.accompanist.WebView
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.accompanist.rememberWebViewNavigator
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.accompanist.rememberWebViewState
import de.tum.informatics.www1.artemis.native_app.feature.login.R
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun Saml2LoginScreen(
    modifier: Modifier,
    viewModel: Saml2LoginViewModel,
    onLoggedIn: () -> Unit
) {
    val saml2LoginResponseDataState by viewModel.saml2LoginResponse.collectAsState()
    val serverUrl: String by viewModel.serverUrl.collectAsState()

    LaunchedEffect(saml2LoginResponseDataState) {
        val currentDataState = saml2LoginResponseDataState
        if (currentDataState is DataState.Success && currentDataState.data == Saml2LoginViewModel.Saml2LoginResponse.Success) {
            onLoggedIn()
        }
    }

    BasicDataStateUi(
        modifier = modifier,
        dataState = saml2LoginResponseDataState,
        loadingText = stringResource(id = R.string.login_saml_screen_login_response_loading),
        failureText = stringResource(id = R.string.login_saml_screen_login_response_failure),
        retryButtonText = stringResource(id = R.string.login_saml_screen_login_response_try_again),
        onClickRetry = viewModel::retryPerformLogin
    ) { loginResponse ->
        when (loginResponse) {
            Saml2LoginViewModel.Saml2LoginResponse.Success -> {
                // Simply display a loading bar. The user is navigated back by the LaunchedEffect
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is Saml2LoginViewModel.Saml2LoginResponse.Error -> {
                when (loginResponse.statusCode) {
                    401 -> {
                        // Display webview for the user to login
                        Saml2LoginWebView(
                            modifier = Modifier.fillMaxSize(),
                            serverUrl = serverUrl,
                            onReceivedAccessToken = { jwt ->
                                viewModel.saveAccessToken(jwt, onLoggedIn)
                            }
                        )
                    }

                    403 -> {
                        // Display forbidden error.
                        ErrorInfo(
                            modifier = Modifier.align(Alignment.Center),
                            errorText = stringResource(
                                id = R.string.login_saml_screen_login_response_forbidden,
                                loginResponse.errorHeader
                            ),
                            onRequestTryAgain = viewModel::retryPerformLogin
                        )
                    }

                    else -> {
                        ErrorInfo(
                            modifier = Modifier.align(Alignment.Center),
                            errorText = stringResource(
                                id = R.string.login_saml_screen_login_response_unexpected_error
                            ),
                            onRequestTryAgain = viewModel::retryPerformLogin
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorInfo(modifier: Modifier, errorText: String, onRequestTryAgain: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = errorText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Button(onClick = onRequestTryAgain) {
            Text(text = stringResource(id = R.string.login_saml_screen_login_response_try_again))
        }
    }
}

private val cookieJwtRegex = "jwt=\"(.*)\"\\w*;".toRegex()

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun Saml2LoginWebView(
    modifier: Modifier,
    serverUrl: String,
    onReceivedAccessToken: (String) -> Unit
) {
    val url = remember(serverUrl) {
        URLBuilder(serverUrl)
            .appendPathSegments("saml2", "authenticate")
            .buildString()
    }

    val cookieManager = remember {
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setCookie(serverUrl, "SAML2flow=true; max-age=120; SameSite=Lax;")
        }
    }

    val navigator = rememberWebViewNavigator()

    val state = rememberWebViewState(url = url)
    Box(modifier = modifier) {
        LaunchedEffect(Unit) {
            while (true) {
                val currentCookie = cookieManager.getCookie(serverUrl)
                cookieJwtRegex.matchEntire(currentCookie)?.let { result ->
                    if (result.groups.size >= 2) {
                        val jwt = result.groups[1]?.value.orEmpty()
                        if (jwt.isNotBlank()) {
                            // Reset cookie
                            cookieManager.setCookie(serverUrl, "")

                            onReceivedAccessToken(jwt)
                            return@LaunchedEffect
                        }
                    }
                }

                delay(1.seconds)
            }
        }

        WebView(
            modifier = Modifier.fillMaxSize(),
            state = state,
            client = Saml2WebClient(),
            onCreated = {
                it.settings.javaScriptEnabled = true
                cookieManager.setAcceptCookie(true)
                cookieManager.setCookie(serverUrl, "SAML2flow=true; max-age=120; SameSite=Lax;")
            }
        )

        if (state.isLoading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()

                Text(
                    text = stringResource(id = R.string.login_saml_screen_login_webview_loading),
                    style = MaterialTheme.typography.bodyLarge
                )

                Button(onClick = navigator::reload) {
                    Text(text = stringResource(id = R.string.login_saml_screen_login_webview_try_again))
                }
            }
        }
    }
}

private class Saml2WebClient : AccompanistWebViewClient()