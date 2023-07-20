package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationConfigurationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.UnsubscribeFromNotificationsWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.UploadPushNotificationDeviceConfigurationWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.WorkManagerPushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.CommunicationNotificationManagerImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.MiscNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.NotificationManagerImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.ReplyWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

val pushModule = module {
    single<PushNotificationJobService> {
        WorkManagerPushNotificationJobService(
            androidContext()
        )
    }

    single<PushNotificationConfigurationService> {
        PushNotificationConfigurationServiceImpl(androidContext(), get())
    }

    single<NotificationManager> { NotificationManagerImpl(get(), get()) }
    single { MiscNotificationManager(androidContext()) }
    single<CommunicationNotificationManager> { CommunicationNotificationManagerImpl(androidContext(), get()) }

    workerOf(::UploadPushNotificationDeviceConfigurationWorker)
    workerOf(::UnsubscribeFromNotificationsWorker)
    workerOf(::ReplyWorker)

    single<NotificationSettingsService> {
        de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.NotificationSettingsServiceImpl(
            get()
        )
    }
    viewModel { PushNotificationSettingsViewModel(get(), get(), get(), get(), get()) }
}