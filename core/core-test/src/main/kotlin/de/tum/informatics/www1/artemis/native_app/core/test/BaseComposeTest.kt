package de.tum.informatics.www1.artemis.native_app.core.test

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModel
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.parameter.ParametersDefinition
import org.koin.mp.KoinPlatformTools
import org.koin.test.KoinTest

abstract class BaseComposeTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = UnconfinedTestDispatcher()

    protected val context: Context get() = InstrumentationRegistry.getInstrumentation().context

    /**
     * Run a block of code with a timeout. The default test timeout is multiplied by the
     * given [timeoutMultiplier].
     * @param timeoutMultiplier The multiplier for the default test timeout. Can be used to increase
     *                          the timeout for flaky tests
     */
    fun <T> runBlockingWithTestTimeout(
        timeoutMultiplier: Int = 1,
        block: suspend () -> T
    ): T {
        return runBlocking {
            withTimeout(DefaultTimeoutMillis * timeoutMultiplier) {
                block()
            }
        }
    }

    /**
     * Test if a ViewModel can be initialized without errors.
     * @param parameters The parameters to pass to the ViewModel constructor
     */
    @OptIn(KoinInternalApi::class)
    inline fun <reified T: ViewModel> ComposeContentTestRule.testViewModelInitialization(
        noinline parameters: ParametersDefinition? = null
    ) {
        setContent {
            // This is a workaround to make koin work in tests.
            // See: https://github.com/InsertKoinIO/koin/issues/1557#issue-1660665501
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                koinViewModel<T>(parameters = parameters)
            }
        }
    }
}