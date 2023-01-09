package de.tum.informatics.www1.artemis.native_app.core.push_notification_settings

import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.service.SettingsService
import de.tum.informatics.www1.artemis.native_app.core.push_notification_settings.service.impl.SettingsServiceImpl
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val pushNotificationModule = module {
    single<SettingsService> { SettingsServiceImpl(get()) }
    viewModelOf(::PushNotificationSettingsViewModel)
}