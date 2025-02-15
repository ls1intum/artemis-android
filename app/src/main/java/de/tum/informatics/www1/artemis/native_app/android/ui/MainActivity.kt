package de.tum.informatics.www1.artemis.native_app.android.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.ui.theme.AppTheme
import de.tum.informatics.www1.artemis.native_app.core.common.withPrevious
import de.tum.informatics.www1.artemis.native_app.core.datastore.AccountService
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.isLoggedIn
import de.tum.informatics.www1.artemis.native_app.core.ui.LinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalLinkOpener
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalWindowSizeClassProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.WindowSizeClassProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.alert.TextAlertDialog
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving.LocalMarkdownLinkResolver
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_images.LocalArtemisImageProvider
import de.tum.informatics.www1.artemis.native_app.feature.dashboard.ui.DashboardScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.LoginScreen
import de.tum.informatics.www1.artemis.native_app.feature.login.navigateToLogin
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.LocalVisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContextManager
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleMetisContextReporter
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.visiblemetiscontextreporter.VisibleStandalonePostDetails
import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.unsubscribeFromNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.compose.koinInject

/**
 * Main and only activity used in the android app.
 * Navigation is handled by decompose and jetpack compose.
 */
class MainActivity : AppCompatActivity(),
    VisibleMetisContextReporter {

    private val serverConfigurationService: ServerConfigurationService = get()
    private val accountService: AccountService = get()
    private val communicationNotificationManager: CommunicationNotificationManager = get()

    override val visibleMetisContexts: MutableStateFlow<List<VisibleMetisContext>> =
        MutableStateFlow(
            emptyList()
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // When the user is logged in, immediately display the course overview.
        val startDestination = runBlocking {
            when (accountService.authenticationData.first()) {
                is AccountService.AuthenticationData.LoggedIn -> DashboardScreen
                AccountService.AuthenticationData.NotLoggedIn -> LoginScreen(null)
            }
        }

        val visibleMetisContextManager = object :
            VisibleMetisContextManager {
            override fun registerMetisContext(metisContext: VisibleMetisContext) {
                visibleMetisContexts.value += metisContext

                cancelCommunicationNotifications(metisContext)
            }

            override fun unregisterMetisContext(metisContext: VisibleMetisContext) {
                visibleMetisContexts.value -= metisContext
            }

            override fun getRegisteredMetisContexts(): List<VisibleMetisContext> {
                return visibleMetisContexts.value
            }
        }

        val (currentHost, isLoggedIn) = runBlocking {
            serverConfigurationService.serverUrl.first()
                .toUri().host to (accountService.authenticationData.first() is AccountService.AuthenticationData.LoggedIn)
        }

        val data = intent?.data
        val newHost = if (data != null && data.scheme == "https") data.host else null

        val hasServerMismatch = newHost != null && newHost != currentHost
        val requiresLogin = hasServerMismatch || !isLoggedIn

        var displayWrongServerDialog by mutableStateOf(hasServerMismatch)

        if (hasServerMismatch || requiresLogin) {
            intent.data = null
        }

        setContent {
            AppTheme {
                CompositionLocalProvider(
                    LocalVisibleMetisContextManager provides visibleMetisContextManager,
                ) {
                    val navController = rememberNavController()

                    val navigateToDeepLinkLogin = {
                        navController.navigateToLogin(nextDestination = data.toString()) {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }

                    LaunchedEffect(hasServerMismatch, requiresLogin) {
                        if (!hasServerMismatch && requiresLogin) {
                            navigateToDeepLinkLogin()
                        }
                    }

                    MainActivityComposeUi(startDestination, navController)

                    if (displayWrongServerDialog) {
                        TextAlertDialog(
                            title = stringResource(id = R.string.deep_link_wrong_host_dialog_title),
                            text = stringResource(
                                id = R.string.deep_link_wrong_host_dialog_message,
                                currentHost.orEmpty(),
                                newHost.orEmpty()
                            ),
                            confirmButtonText = stringResource(id = R.string.deep_link_wrong_host_dialog_positive),
                            dismissButtonText = stringResource(id = R.string.deep_link_wrong_host_dialog_negative),
                            onPressPositiveButton = {
                                lifecycleScope.launch {
                                    if (data != null) {
                                        unsubscribeFromNotifications(get(), get())
                                        accountService.logout()

                                        val newUrl = data.scheme + "://" + data.host.orEmpty()
                                        serverConfigurationService.updateServerUrl(newUrl)

                                        navigateToDeepLinkLogin()

                                        displayWrongServerDialog = false
                                    }
                                }
                            },
                            onDismissRequest = { displayWrongServerDialog = false }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun MainActivityComposeUi(startDestination: Any, navController: NavHostController) {
        // Listen for when the user get logged out (e.g. because their token has expired)
        // This only happens when the user has the app running for multiple days or the user logged out manually
        LaunchedEffect(Unit) {
            accountService.authenticationData.withPrevious()
                .map { (prev, now) -> prev?.isLoggedIn to now.isLoggedIn }
                .collect { (wasLoggedIn, isLoggedIn) ->
                    if (wasLoggedIn == true && !isLoggedIn) {
                        navController.navigateToLogin {
                            popUpTo(DashboardScreen) {
                                inclusive = true
                            }
                        }
                    }
                }
        }

        val windowSizeClassProvider = remember {
            object : WindowSizeClassProvider {
                @Composable
                override fun provideWindowSizeClass(): WindowSizeClass =
                    calculateWindowSizeClass(
                        activity = this@MainActivity
                    )
            }
        }

        val linkOpener = remember {
            CustomTabsLinkOpener(this@MainActivity)
        }

        CompositionLocalProvider(
            LocalWindowSizeClassProvider provides windowSizeClassProvider,
            LocalLinkOpener provides linkOpener,
            LocalArtemisImageProvider provides koinInject(),
            LocalMarkdownLinkResolver provides koinInject()
        ) {
            NavHost(navController = navController, startDestination = startDestination) {
                rootNavGraph(
                    navController = navController,
                    onDisplayThirdPartyLicenses = {
                        val intent =
                            Intent(this@MainActivity, OssLicensesMenuActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private class CustomTabsLinkOpener(private val context: Context) : LinkOpener {
        override fun openLink(url: String) {
            CustomTabsIntent.Builder().build()
                .launchUrl(context, Uri.parse(url))
        }
    }

    override fun onResume() {
        super.onResume()

        visibleMetisContexts.value.forEach(::cancelCommunicationNotifications)
    }

    private fun cancelCommunicationNotifications(visibleMetisContext: VisibleMetisContext) {
        if (visibleMetisContext is VisibleStandalonePostDetails) {
            val parentId = visibleMetisContext.postId

            lifecycleScope.launch {
                communicationNotificationManager.deleteCommunication(parentId)
            }
        }
    }
}
