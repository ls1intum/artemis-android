package de.tum.informatics.www1.artemis.native_app.feature.login

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.test.testDataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.ServerConfigurationService
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.LoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.ServerProfileInfoServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Category(EndToEndTest::class)
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
    val composeTestRule = createComposeRule()

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
        ShadowLog.stream = System.out

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        startKoin {
            androidContext(context)

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

        val context = InstrumentationRegistry.getInstrumentation().targetContext

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
            context.getString(R.string.login_username_label)
        )
            .performTextInput(username)

        composeTestRule.onNodeWithText(
            context.getString(R.string.login_password_label)
        )
            .performTextInput(password)

        composeTestRule
            .onNodeWithText(context.getString(R.string.login_perform_login_button_text))
            .performClick()

        composeTestRule.waitUntil { successfullyLoggedIn }

        assertTrue(successfullyLoggedIn)
    }
}