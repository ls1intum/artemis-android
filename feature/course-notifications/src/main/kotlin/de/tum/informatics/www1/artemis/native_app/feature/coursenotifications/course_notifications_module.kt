package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationSettingsService
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.impl.CourseNotificationSettingsServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val courseNotificationsModule = module {
    single<CourseNotificationSettingsService> { CourseNotificationSettingsServiceImpl(get(), get()) }

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