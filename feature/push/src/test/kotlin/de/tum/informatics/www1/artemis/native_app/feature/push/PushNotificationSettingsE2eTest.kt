package de.tum.informatics.www1.artemis.native_app.feature.push

import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.NotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.impl.NotificationSettingsServiceImpl
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
@Ignore("The notifications have switched to a course-specific system with Artemis 8.0, see https://github.com/ls1intum/artemis-android/issues/562")
class PushNotificationSettingsE2eTest : BaseComposeTest() {

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, pushModule, testLoginModule, module {
            single<PushNotificationConfigurationService> { FakePushNotificationConfigurationService() }
        })
    }

    private lateinit var accessToken: String

    @Before
    fun setup() {
        ShadowLog.stream = System.out

        accessToken = runBlockingWithTestTimeout { performTestLogin() }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can register for and unregister from push notifications`() {
        val pushNotificationSettingsService: NotificationSettingsService =
            NotificationSettingsServiceImpl(get())

        runBlockingWithTestTimeout {
            pushNotificationSettingsService.uploadPushNotificationDeviceConfigurationsToServer(
                serverUrl = testServerUrl,
                authToken = accessToken,
                firebaseToken = generateId(),
                appVersion = NormalizedAppVersion.ZERO
            ).orThrow("Could not register for push notifications")

            pushNotificationSettingsService.unsubscribeFromNotifications(
                serverUrl = testServerUrl,
                authToken = accessToken,
                firebaseToken = generateId()
            ).orThrow("Could not unregister from push notifications")
        }
    }

}