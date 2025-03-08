package de.tum.informatics.www1.artemis.native_app.feature.push.ui.model

import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import de.tum.informatics.www1.artemis.native_app.core.model.account.AccountAuthority
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.model.PushNotificationSettingUtil.filterByAuthorities
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Category(UnitTest::class)
@RunWith(RobolectricTestRunner::class)
class PushNotificationSettingUtilTest {

    @Test
    fun `test filterByAuthorities`() {
        val settings = listOf(
            PushNotificationSettingsViewModel.NotificationCategory("tutor-notification", emptyList()),
            PushNotificationSettingsViewModel.NotificationCategory("editor-notification", emptyList()),
            PushNotificationSettingsViewModel.NotificationCategory("instructor-notification", emptyList()),
            PushNotificationSettingsViewModel.NotificationCategory("general-notification", emptyList())

        )

        val authorities = listOf(AccountAuthority.ROLE_TA, AccountAuthority.ROLE_EDITOR)

        val filteredSettings = settings.filterByAuthorities(authorities)

        val expectedSettings = listOf(
            PushNotificationSettingsViewModel.NotificationCategory("tutor-notification", emptyList()),
            PushNotificationSettingsViewModel.NotificationCategory("editor-notification", emptyList()),
            PushNotificationSettingsViewModel.NotificationCategory("general-notification", emptyList())
        )

        assertEquals(expectedSettings, filteredSettings)
    }

}