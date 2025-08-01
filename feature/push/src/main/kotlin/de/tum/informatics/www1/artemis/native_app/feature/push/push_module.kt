package de.tum.informatics.www1.artemis.native_app.feature.push

import de.tum.informatics.www1.artemis.native_app.feature.push.service.CommunicationNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.NotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationCipher
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationHandler
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationCipherImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationConfigurationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.PushNotificationHandlerImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.UnsubscribeFromNotificationsWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.UploadPushNotificationDeviceConfigurationWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.WorkManagerPushNotificationJobService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.CommunicationNotificationManagerImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.GeneralNotificationManager
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.NotificationManagerImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mark_as_read.MarkConversationAsReadWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.mute.MuteConversationWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.impl.notification_manager.reply.UpdateReplyNotificationWorker
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.NotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.push.service.network.impl.NotificationSettingsServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.push.ui.PushNotificationSettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pushModule = module {
    single<PushNotificationJobService> {
        WorkManagerPushNotificationJobService(
            androidContext(), get()
        )
    }

    single<PushNotificationConfigurationService> {
        PushNotificationConfigurationServiceImpl(androidContext(), get(), get())
    }

    single<NotificationManager> { NotificationManagerImpl(get(), get()) }
    single { GeneralNotificationManager(androidContext()) }
    single<CommunicationNotificationManager> {
        CommunicationNotificationManagerImpl(
            androidContext(),
            get(),
            get(),
            get()
        )
    }
    single<PushNotificationCipher> { PushNotificationCipherImpl(get()) }
    single<PushNotificationHandler> { PushNotificationHandlerImpl(androidApplication(), get()) }

    workerOf(::UploadPushNotificationDeviceConfigurationWorker)
    workerOf(::UnsubscribeFromNotificationsWorker)
    workerOf(::UpdateReplyNotificationWorker)
    workerOf(::MarkConversationAsReadWorker)
    workerOf(::MuteConversationWorker)

    single<NotificationSettingsService> {
        NotificationSettingsServiceImpl(
            get()
        )
    }
    viewModel { PushNotificationSettingsViewModel(get(), get(), get()) }}
