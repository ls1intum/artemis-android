package de.tum.informatics.www1.artemis.native_app.feature.login

import de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection.CustomInstanceSelectionViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerNotificationStorageService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.PersistentServerNotificationStorageService
import org.koin.android.ext.koin.androidContext

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    viewModelOf(::AccountViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::CustomInstanceSelectionViewModel)

    single<ServerNotificationStorageService> {
        PersistentServerNotificationStorageService(androidContext())
    }
}