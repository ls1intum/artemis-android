package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.cookieAuth
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.course_creation.ktorProvider
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterUi
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_CONFIRM_PASSWORD
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_EMAIL
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_FIRST_NAME
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_LAST_NAME
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_LOGIN_NAME
import de.tum.informatics.www1.artemis.native_app.feature.login.register.TEST_TAG_TEXT_FIELD_PASSWORD
import de.tum.informatics.www1.artemis.native_app.feature.login.test.getAdminAccessToken
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(RobolectricTestRunner::class)
@Category(EndToEndTest::class)
class RegisterEndToEndTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, pushModule, testLoginModule)
    }

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
            networkStatusProvider = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            RegisterUi(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                viewModel = viewModel,
                onRegistered = { }
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

        runBlockingWithTestTimeout {
            val result = viewModel.register().await()
            assertEquals(result, RegisterViewModel.RegistrationResponse.SUCCESS,
                "Registration failed: $result"
            )

            val users: List<User> = getUsers(loginName)
            println("Loaded users are $users")

            assertTrue(users.any { it.username == loginName }, "Could not find registered user")
        }
    }

    private suspend fun getUsers(loginName: String): List<User> {
        return ktorProvider.ktorClient.get(testServerUrl) {
            url {
                appendPathSegments("api", "admin", "users")

                parameter("page", 0)
                parameter("pageSize", 10)
                parameter("searchTerm", loginName)
                parameter("sortingOrder", "ASCENDING")
                parameter("sortedColumn", "id")
                parameter("filters", "")
                parameter("authorities", "")
                parameter("origins", "")
                parameter("registrationNumbers", "")
                parameter("status", "")
                parameter("courseIds", "")
            }

            cookieAuth(getAdminAccessToken())
            contentType(ContentType.Application.Json)
        }.body()
    }
}
