package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.Constants
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.ktorProvider
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.ui.common.TEST_TAG_BUTTON_WITH_LOADING_ANIMATION_LOADING
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterUi
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_CONFIRM_PASSWORD
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_EMAIL
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_FIRST_NAME
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_LAST_NAME
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_LOGIN_NAME
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_PASSWORD
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import io.ktor.client.request.put
import io.ktor.http.appendPathSegments
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Category(EndToEndTest::class)
class RegisterEndToEndTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, pushModule)
    }

    @Ignore("Server throws an exception as mail service does not work.")
    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can register to server`() {
        val id = generateId().take(10)
        val firstName = "fn$id"
        val lastName = "ln$id"
        val loginName = "login$id"
        val email = "u_$id@tum.de"
        val password = "pw_$id"

        val viewModel = RegisterViewModel(
            savedStateHandle = SavedStateHandle(),
            registerService = get(),
            serverConfigurationService = get(),
            serverProfileInfoService = get(),
            networkStatusProvider = get()
        )

        var hasRegistered = false

        composeTestRule.setContent {
            RegisterUi(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                viewModel = viewModel,
                onRegistered = { hasRegistered = true }
            )
        }

        val performTextInput = { testTag: String, input: String ->
            composeTestRule
                .onNode(
                    hasSetTextAction() and hasParent(hasTestTag(testTag)),
                    useUnmergedTree = true
                )
                .performScrollTo()
                .performTextInput(input)
        }

        performTextInput(TEST_TAG_TEXT_FIELD_FIRST_NAME, firstName)
        performTextInput(TEST_TAG_TEXT_FIELD_LAST_NAME, lastName)
        performTextInput(TEST_TAG_TEXT_FIELD_LOGIN_NAME, loginName)
        performTextInput(TEST_TAG_TEXT_FIELD_EMAIL, email)
        performTextInput(TEST_TAG_TEXT_FIELD_PASSWORD, password)
        performTextInput(TEST_TAG_TEXT_FIELD_CONFIRM_PASSWORD, password)

        composeTestRule
            .onNodeWithText(context.getString(R.string.register_button_register))
            .assertIsEnabled()
            .performScrollTo()
            .performClick()


        composeTestRule.waitUntil { hasRegistered }
    }
}
