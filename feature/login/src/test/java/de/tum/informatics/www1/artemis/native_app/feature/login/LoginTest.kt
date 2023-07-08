package de.tum.informatics.www1.artemis.native_app.feature.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.test.testDataModule
import de.tum.informatics.www1.artemis.native_app.core.datastore.datastoreModule
import de.tum.informatics.www1.artemis.native_app.core.device.deviceModule
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.ui.uiModule
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginUi
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.LoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.ServerProfileInfoServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Password
import de.tum.informatics.www1.artemis.native_app.feature.login.test.user1Username
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
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
import org.koin.test.KoinTestRule
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

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)
        modules(coreTestModules)
        modules(loginModule, pushModule)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `test login is successful`() {
        Log.i(
            TAG,
            "Logging in with user $user1Username and $user1Password to server $testServerUrl"
        )

        val viewModel = LoginViewModel(
            savedStateHandle = SavedStateHandle(),
            accountService = get(),
            loginService = get(),
            pushNotificationConfigurationService = get(),
            serverConfigurationService = get(),
            serverProfileInfoService = get(),
            networkStatusProvider = get(),
            coroutineContext = UnconfinedTestDispatcher()
        )

        var successfullyLoggedIn = false

        composeTestRule.setContent {
            LoginUi(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
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
    }
}