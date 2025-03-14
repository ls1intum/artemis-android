package de.tum.informatics.www1.artemis.native_app.feature.push.ui.model

import de.tum.informatics.www1.artemis.native_app.core.model.account.AccountAuthority
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel

object PushNotificationSettingUtil {

    /**
     * These settings can not be edited by the user
     */
    private val nonEditableSettingIds = listOf(
        "notification.user-notification.vcs-access-token-added",
        "notification.user-notification.vcs-access-token-expired",
        "notification.user-notification.vcs-access-token-expires-soon",
        "notification.user-notification.ssh-key-added",
        "notification.user-notification.ssh-key-expires-soon",
        "notification.user-notification.ssh-key-has-expired",
        "notification.user-notification.data-export-created",
        "notification.user-notification.data-export-failed"
    )

    private val restrictedCategoryIdByAuthority: Map<AccountAuthority, String> = mapOf(
        AccountAuthority.ROLE_TA to "tutor-notification",
        AccountAuthority.ROLE_EDITOR to "editor-notification",
        AccountAuthority.ROLE_INSTRUCTOR to "instructor-notification",
    )

    internal fun List<PushNotificationSetting>.filterEditable() =
        filterNot { nonEditableSettingIds.contains(it.settingId) }

    internal fun List<PushNotificationSettingsViewModel.NotificationCategory>.filterByAuthorities(
        authorities: List<AccountAuthority>
    ) = filter {
        val availableCategoryIds = authorities.map { authority ->
            restrictedCategoryIdByAuthority[authority]
        }.toSet()

        val forbiddenCategoryIds = restrictedCategoryIdByAuthority.values - availableCategoryIds

        it.categoryId !in forbiddenCategoryIds
    }

}