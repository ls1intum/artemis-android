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
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.setTestServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.LoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.ServerProfileInfoServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Password
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Category(EndToEndTest::class)
class LoginTest : KoinTest {

    companion object {
        private const val TAG = "LoginTest"
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val loginModule = module {
        single<LoginService> { LoginServiceImpl(get()) }
        single<ServerProfileInfoService> { ServerProfileInfoServiceImpl(get()) }

        viewModel {
            LoginViewModel(
                savedStateHandle = get(),
                accountService = get(),
                loginService = get(),
                pushNotificationConfigurationService = get(),
                serverConfigurationService = get(),
                serverProfileInfoService = get(),
                networkStatusProvider = get(),
                coroutineContext = UnconfinedTestDispatcher()
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

        runBlocking {
            setTestServerUrl()
        }
    }

    @Test
    fun `test login is successful`() {
        Log.i(TAG, "Logging in with user $user1Username and $user1Password to server $testServerUrl")

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
            .performTextInput(user1Username)

        composeTestRule.onNodeWithText(
            context.getString(R.string.login_password_label)
        )
            .performTextInput(user1Password)

        composeTestRule
            .onNodeWithText(context.getString(R.string.login_perform_login_button_text))
            .performClick()

        composeTestRule.waitUntil(DefaultTimeoutMillis) { successfullyLoggedIn }

        assertTrue(successfullyLoggedIn)
    }
}