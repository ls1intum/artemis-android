package de.tum.informatics.www1.artemis.native_app.feature.coursenotifications

import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.CourseNotificationService
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.service.impl.CourseNotificationServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.coursenotifications.ui.settings.CourseNotificationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val courseNotificationsModule = module {
    viewModel { CourseNotificationViewModel(get(), get(), get(), get(), get()) }
    single<CourseNotificationService> { CourseNotificationServiceImpl(get(), get()) }
}