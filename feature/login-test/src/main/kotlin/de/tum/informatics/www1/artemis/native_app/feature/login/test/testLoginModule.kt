package de.tum.informatics.www1.artemis.native_app.feature.login.test

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val testLoginModule = module {
    singleOf(::AdminLoginService)
}