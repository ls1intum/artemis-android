package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationConfigurationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.UnsubscribeFromNotificationsWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.UploadPushNotificationDeviceConfigurationWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.WorkManagerPushNotificationJobService
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

    workerOf(::UploadPushNotificationDeviceConfigurationWorker)
    workerOf(::UnsubscribeFromNotificationsWorker)
    workerOf(::ReplyWorker)

    single<de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationSettingsService> {
        de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.NotificationSettingsServiceImpl(
            get()
        )
    }
    viewModel { PushNotificationSettingsViewModel(get(), get(), get(), get(), get()) }
}