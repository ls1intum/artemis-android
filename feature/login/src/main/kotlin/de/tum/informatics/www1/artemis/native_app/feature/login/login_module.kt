package de.tum.informatics.www1.artemis.native_app.feature.login

import de.tum.informatics.www1.artemis.native_app.feature.login.login.LoginViewModel
import de.tum.informatics.www1.artemis.native_app.feature.login.register.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val loginModule = module {
    viewModelOf(::AccountViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}