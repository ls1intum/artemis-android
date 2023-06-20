package de.tum.informatics.www1.artemis.native_app.feature.login

import de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection.CustomInstanceSelectionViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.RegisterService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerNotificationStorageService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerProfileInfoService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.LoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.PersistentServerNotificationStorageService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.RegisterServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.ServerProfileInfoServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.coroutines.EmptyCoroutineContext

val loginModule = module {
    viewModelOf(::AccountViewModel)
    viewModel {
        LoginViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            EmptyCoroutineContext
        )
    }
    viewModelOf(::RegisterViewModel)
    viewModelOf(::CustomInstanceSelectionViewModel)
    viewModel { params -> Saml2LoginViewModel(params.get(), get(), get(), get(), get()) }

    single<ServerNotificationStorageService> {
        PersistentServerNotificationStorageService(androidContext())
    }

    single<RegisterService> { RegisterServiceImpl(get()) }

    single<LoginService> { LoginServiceImpl(get()) }
    single<ServerProfileInfoService> { ServerProfileInfoServiceImpl(get()) }
}