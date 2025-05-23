package de.tum.informatics.www1.artemis.native_app.feature.login

import de.tum.informatics.www1.artemis.native_app.feature.login.custom_instance_selection.CustomInstanceSelectionViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.saml2_login.Saml2LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.service.AndroidCredentialService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.ServerNotificationStorageService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.AndroidCredentialServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.impl.PersistentServerNotificationStorageService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.LoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.PasskeyLoginService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.RegisterService
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl.LoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl.PasskeyLoginServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.login.service.network.impl.RegisterServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    viewModelOf(::AccountViewModel)
    viewModel {
        LoginViewModel(
            savedStateHandle = get(),
            accountService = get(),
            loginService = get(),
            pushNotificationConfigurationService = get(),
            serverConfigurationService = get(),
            serverProfileInfoService = get(),
            networkStatusProvider = get(),
            passkeyLoginService = get(),
            androidCredentialService = get(),
        )
    }
    viewModel {
        RegisterViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModelOf(::CustomInstanceSelectionViewModel)
    viewModel { params -> Saml2LoginViewModel(params.get(), get(), get(), get(), get()) }

    single<ServerNotificationStorageService> {
        PersistentServerNotificationStorageService(androidContext())
    }

    single<RegisterService> { RegisterServiceImpl(get()) }

    single<LoginService> { LoginServiceImpl(get()) }

    single<AndroidCredentialService> {
        AndroidCredentialServiceImpl(
            androidContext()
        )
    }
    single<PasskeyLoginService> {
        PasskeyLoginServiceImpl(
            get(),
            get()
        )
    }
}