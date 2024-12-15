package de.tum.informatics.www1.artemis.native_app.core.test

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.withTimeout
import org.junit.Rule
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
}