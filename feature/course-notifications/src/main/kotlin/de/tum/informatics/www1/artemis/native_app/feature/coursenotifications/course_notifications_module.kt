package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationService
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationSettingsServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.impl.CourseNotificationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.notification.CourseNotificationViewModel

val courseNotificationsModule = module {
    single<CourseNotificationService> { CourseNotificationServiceImpl(get(), get()) }
    single<CourseNotificationSettingsService> { CourseNotificationSettingsServiceImpl(get(), get()) }

    viewModel { CourseNotificationViewModel(get(), get(), get(), get(), get()) }

    viewModel { (courseId: Long) ->
        CourseNotificationSettingsViewModel(
            courseId = courseId,
            courseNotificationSettingsService = get(),
            serverConfigurationService = get(),
            accountService = get(),
            networkStatusProvider = get()
        )
    }
}