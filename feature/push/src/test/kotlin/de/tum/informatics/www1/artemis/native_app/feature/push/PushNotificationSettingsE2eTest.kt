package de.tum.informatics.www1.artemis.native_app.feature.push

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest
import de.tum.informatics.www1.artemis.native_app.core.data.filterSuccess
import de.tum.informatics.www1.artemis.native_app.core.test.coreTestModules
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTestTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.DefaultTimeoutMillis
import de.tum.informatics.www1.artemis.native_app.core.test.test_setup.testServerUrl
import de.tum.informatics.www1.artemis.native_app.feature.login.loginModule
import de.tum.informatics.www1.artemis.native_app.feature.login.test.performTestLogin
import de.tum.informatics.www1.artemis.native_app.feature.login.test.testLoginModule
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsUi
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.TEST_TAG_PUSH_CHECK_BOX
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.group
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.testTagForSetting
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.testTagForSettingCategory
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@Category(EndToEndTest::class)
@RunWith(RobolectricTestRunner::class)
class PushNotificationSettingsE2eTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

        accessToken = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                performTestLogin()
            }
        }
    }

    @Test(timeout = DefaultTestTimeoutMillis)
    fun `can retrieve notification settings`() {
        val dispatcher = UnconfinedTestDispatcher()
        val viewModel = setupUiAndViewModel(dispatcher)

        dispatcher.scheduler.advanceUntilIdle()

        val currentSettingsByGroup = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                viewModel
                    .currentSettingsByGroup
                    .filterSuccess()
                    .first()
            }
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
        val dispatcher = UnconfinedTestDispatcher()
        val viewModel = setupUiAndViewModel(dispatcher)

        dispatcher.scheduler.advanceUntilIdle()

        val currentSettingsByGroup = runBlocking {
            withTimeout(DefaultTimeoutMillis) {
                viewModel
                    .currentSettingsByGroup
                    .filterSuccess()
                    .first()
            }
        }

        val category =
            currentSettingsByGroup.first { category -> category.settings.isNotEmpty() && category.settings.any { it.push != null } }
        val setting = category.settings.first { it.push != null }

        // Click on push checkbox of setting
        composeTestRule
            .onNode(
                hasAnyAncestor(hasTestTag(testTagForSettingCategory(category.categoryId)))
                        and hasAnyAncestor(hasTestTag(testTagForSetting(setting.settingId)))
                        and hasAnyAncestor(hasTestTag(TEST_TAG_PUSH_CHECK_BOX))
                        and hasClickAction()
            )
            .performScrollTo()
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

        val updatedSetting = updatedSettings
            .first { it.group == setting.group && it.id == setting.id }

        // Check that the new setting has been updated correctly
        assertEquals(!setting.push!!, updatedSetting.push!!)
    }

    @OptIn(KoinInternalApi::class)
    private fun setupUiAndViewModel(testDispatcher: TestDispatcher = UnconfinedTestDispatcher()): PushNotificationSettingsViewModel {
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