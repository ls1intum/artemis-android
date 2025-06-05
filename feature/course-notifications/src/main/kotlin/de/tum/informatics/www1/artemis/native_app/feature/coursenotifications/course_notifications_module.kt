package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val courseNotificationsModule = module {
    viewModel { CourseNotificationViewModel(get(), get(), get(), get(), get()) }
    single<CourseNotificationService> { CourseNotificationServiceImpl(get(), get()) }
}