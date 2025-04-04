package de.tum.informatics.www1.artemis.native_app.feature.coremodulestest

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.dataModule
import de.tum.informatics.www1.artemis.native_app.core.data.service.network.ServerTimeService
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.test.KoinTestRule
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class SyncServerTimeE2eTest : BaseComposeTest() {

    @get:Rule
    val koinRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, dataModule, testLoginModule)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `sync server time`() = runTest(timeout = DefaultTimeoutMillis.milliseconds * 2) {       // Multiplied by 2, because flaky test
        val serverTimeService: ServerTimeService = koinRule.koin.get()

        performTestLogin()

        val secondClock = async {
            serverTimeService.getServerClock()
                .drop(3).first()
        }

        testScheduler.advanceTimeBy(5.seconds)

        secondClock.await()
    }
}