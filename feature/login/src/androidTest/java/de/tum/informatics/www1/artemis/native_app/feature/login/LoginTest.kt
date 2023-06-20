package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class LoginTest : KoinTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val trustAll = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

    }

    private val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf(trustAll), SecureRandom())
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json)
        }

        engine {
            https {
                trustManager = trustAll
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 1.seconds.inWholeMilliseconds
            connectTimeoutMillis = 1.seconds.inWholeMilliseconds
            socketTimeoutMillis = 1.seconds.inWholeMilliseconds
        }
    }

    private val dataModule = module {
        singleOf(::JsonProvider)
        single<KtorProvider> {
            object : KtorProvider {
                override val ktorClient: HttpClient = httpClient
            }
        }
    }

//    private val loginModule = module {
//        single<LoginService> { LoginServiceImpl(get()) }
//        single<ServerProfileInfoService> { ServerProfileInfoServiceImpl(get()) }
//
//        viewModel {
//            LoginViewModel(
//                get(),
//                get(),
//                get(),
//                get(),
//                get(),
//                get(),
//                get(),
//                UnconfinedTestDispatcher()
//            )
//        }
//    }

    @Test
    fun testLogin() {
        startKoin {
            androidContext(composeTestRule.activity)

            modules(
                dataModule,
                uiModule,
                datastoreModule,
                deviceModule,
                websocketModule,
                pushModule,
                loginModule
            )
        }

        val serverConfigurationService: ServerConfigurationService = get()
        runBlocking {
            serverConfigurationService.updateServerUrl("https://192.168.178.65")
        }

        var successfullyLoggedIn = false

        composeTestRule.setContent {
            LoginUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = koinViewModel(),
                onLoggedIn = {
                    successfullyLoggedIn = true
                },
                onClickSaml2Login = {}
            )
        }

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login_username_label)
        )
            .performTextInput("artemis_test_user_4")

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login_password_label)
        )
            .performTextInput("ArTEMiS_4_pw2017")

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.login_perform_login_button_text))
            .performClick()

        runBlocking {
            composeTestRule.awaitIdle()
        }

        composeTestRule.waitUntil { successfullyLoggedIn }

        assertTrue(successfullyLoggedIn)
    }
}