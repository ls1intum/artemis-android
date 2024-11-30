package de.tum.informatics.www1.artemis.native_app.feature.login

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection.CustomInstanceSelectionViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.pushModule
import org.junit.Rule
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test


@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
internal class login_moduleTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, pushModule)
    }


    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the loginModule initializes AccountViewModel without errors`() {
        composeTestRule.testViewModelInitialization<AccountViewModel>()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the loginModule initializes LoginViewModel without errors`() {
        composeTestRule.testViewModelInitialization<LoginViewModel>()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the loginModule initializes RegisterViewModel without errors`() {
        composeTestRule.testViewModelInitialization<RegisterViewModel>()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the loginModule initializes CustomInstanceSelectionViewModel without errors`() {
        composeTestRule.testViewModelInitialization<CustomInstanceSelectionViewModel>()
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `the loginModule initializes Saml2LoginViewModel without errors`() {
        composeTestRule.testViewModelInitialization<Saml2LoginViewModel> {
            parametersOf(true)  // rememberMe
        }
    }
}