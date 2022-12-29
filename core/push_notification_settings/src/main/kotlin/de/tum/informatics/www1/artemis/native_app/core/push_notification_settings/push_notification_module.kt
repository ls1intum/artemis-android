package de.tum.informatics.www1.artemis.native_app.core.push_notification_settings

import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.service.PushNotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.service.impl.PushNotificationSettingsServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val pushNotificationModule = module {
    single<PushNotificationSettingsService> { PushNotificationSettingsServiceImpl(get()) }
    viewModelOf(::PushNotificationSettingsViewModel)
}