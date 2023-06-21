package de.tum.informatics.www1.artemis.native_app.feature.login

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import de.tum.informatics.www1.artemis.native_app.core.data.service.KtorProvider
import de.tum.informatics.www1.artemis.native_app.core.data.service.impl.JsonProvider
import de.tum.informatics.www1.artemis.native_app.core.data.testDataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.core.websocket.websocketModule
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.LoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.ServerProfileInfoServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class LoginTest : KoinTest {

    companion object {
        private const val TAG = "LoginTest"
    }

    private val username: String
        get() = System.getenv("username") ?: "test_user"

    private val password: String
        get() = System.getenv("password") ?: "test_user_password"

    private val serverUrl: String
        get() = System.getenv("serverUrl") ?: "https://localhost"

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val loginModule = module {
        single<LoginService> { LoginServiceImpl(get()) }
        single<ServerProfileInfoService> { ServerProfileInfoServiceImpl(get()) }

        viewModel {
            LoginViewModel(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                UnconfinedTestDispatcher()
            )
        }
    }

    @Before
    fun setupTest() {
        startKoin {
            androidContext(composeTestRule.activity)

            modules(
                testDataModule,
                uiModule,
                datastoreModule,
                deviceModule,
                pushModule,
                loginModule
            )
        }

        val serverConfigurationService: ServerConfigurationService = get()
        runBlocking {
            serverConfigurationService.updateServerUrl(serverUrl)
        }
    }

    @Test
    fun `test login is successful`() {
        Log.i(TAG, "Logging in with user $username and $password to server $serverUrl")

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
            .performTextInput(username)

        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.login_password_label)
        )
            .performTextInput(password)

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.login_perform_login_button_text))
            .performClick()

        composeTestRule.waitUntil { successfullyLoggedIn }

        assertTrue(successfullyLoggedIn)
    }
}