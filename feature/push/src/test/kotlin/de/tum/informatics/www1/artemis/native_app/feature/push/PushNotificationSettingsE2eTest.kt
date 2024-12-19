package de.tum.informatics.www1.artemis.native_app.feature.push

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.common.test.testServerUrl
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.test.BaseComposeTest
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.generateId
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.NotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.impl.NotificationSettingsServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsUi
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.group
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.testTagForSetting
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.testTagForSettingCategory
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.testTagForSwitch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import org.robolectric.util.Logger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class PushNotificationSettingsE2eTest : BaseComposeTest() {

    companion object {
        private const val TAG = "PushNotificationSettingsE2eTest"
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        androidContext(InstrumentationRegistry.getInstrumentation().context)

        modules(coreTestModules)
        modules(loginModule, pushModule, testLoginModule, module {
            single<PushNotificationConfigurationService> { FakePushNotificationConfigurationService() }
        })
    }

    private val notificationSettingsService: NotificationSettingsService get() = get()

    private lateinit var accessToken: String

    @Before
    fun setup() {
        ShadowLog.stream = System.out

        accessToken = runBlockingWithTestTimeout { performTestLogin() }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can retrieve notification settings`() {
        val viewModel = setupUiAndViewModel()

        val currentSettingsByGroup = runBlockingWithTestTimeout {
            viewModel
                .currentSettingsByGroup
                .filterSuccess()
                .first()
        }

        assertTrue(
            currentSettingsByGroup.isNotEmpty(),
            "Loaded notification settings are empty. They should not be empty! Did the API change?"
        )

        currentSettingsByGroup.forEach { category ->
            category.settings.forEach { setting ->
                val categoryMatcher = hasTestTag(testTagForSettingCategory(category.categoryId))

                composeTestRule
                    .waitUntilAtLeastOneExists(categoryMatcher, DefaultTestTimeoutMillis)

                composeTestRule
                    .onNode(
                        hasAnyAncestor(categoryMatcher) and hasTestTag(
                            testTagForSetting(setting.settingId)
                        )
                    )
                    .assertExists()
            }
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can update notification settings`() {
        val viewModel = setupUiAndViewModel()

        val currentSettingsByGroup = runBlockingWithTestTimeout {
            viewModel
                .currentSettingsByGroup
                .filterSuccess()
                .first()
        }

        Logger.info("CurrentSettingsByGroup=$currentSettingsByGroup")

        val category =
            currentSettingsByGroup.first { category -> category.settings.isNotEmpty() && category.settings.any { it.push != null } }

        Logger.info("Selected category for switch=$category")

        val setting = category.settings.first { it.push != null }

        Logger.info("Selected setting for switch=$setting")

        // Click on push switch of setting
        composeTestRule.onNodeWithTag(testTagForSwitch(setting.settingId))
            .assertHasClickAction()
            .performClick()

        val saveSettingsResult = runBlocking {
            withTimeoutOrNull(DefaultTimeoutMillis) {
                viewModel.isDirty.filter { it }.first()
            } ?: throw RuntimeException("is dirty is not set to true")

            withTimeout(DefaultTimeoutMillis) {
                viewModel.saveSettings().await()
            }
        }

        assertTrue(saveSettingsResult, "Settings were not saved successfully")

        val updatedSettings = runBlocking {
            withTimeoutOrNull(DefaultTimeoutMillis) {
                notificationSettingsService.getNotificationSettings(
                    testServerUrl,
                    accessToken
                ).orNull()
            } ?: throw RuntimeException("Could not load updated notification settings")
        }

        Logger.info("Updated settings=$updatedSettings")

        val updatedSetting = updatedSettings
            .first { it.group == setting.group && it.settingId == setting.settingId }

        Logger.info("Updated setting=$updatedSetting")

        // Check that the new setting has been updated correctly
        assertEquals(!setting.push!!, updatedSetting.push!!)
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can register for and unregister from push notifications`() {
        val pushNotificationSettingsService: NotificationSettingsService =
            NotificationSettingsServiceImpl(get())

        runBlockingWithTestTimeout {
            pushNotificationSettingsService.uploadPushNotificationDeviceConfigurationsToServer(
                serverUrl = testServerUrl,
                authToken = accessToken,
                firebaseToken = generateId()
            ).orThrow("Could not register for push notifications")

            pushNotificationSettingsService.unsubscribeFromNotifications(
                serverUrl = testServerUrl,
                authToken = accessToken,
                firebaseToken = generateId()
            ).orThrow("Could not unregister from push notifications")
        }
    }

    @OptIn(KoinInternalApi::class)
    private fun setupUiAndViewModel(): PushNotificationSettingsViewModel {
        val viewModel = PushNotificationSettingsViewModel(
            notificationSettingsService = get(),
            networkStatusProvider = get(),
            serverConfigurationService = get(),
            accountService = get(),
            pushNotificationConfigurationService = get(),
            coroutineContext = testDispatcher
        )

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                PushNotificationSettingsUi(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }

        return viewModel
    }
}