package de.tum.informatics.www1.artemis.native_app.feature.settings

import de.tum.informatics.www1.artemis.native_app.feature.settings.service.ChangeProfilePictureService
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.PasskeySettingsService
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.impl.ChangeProfilePictureServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.settings.service.impl.PasskeySettingsServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single<ChangeProfilePictureService> { ChangeProfilePictureServiceImpl(get(), get()) }
    single<PasskeySettingsService> { PasskeySettingsServiceImpl(get(), get()) }

    viewModel {
        SettingsViewModel(
            accountService = get(),
            accountDataService = get(),
            networkStatusProvider = get(),
            pushNotificationJobService = get(),
            pushNotificationConfigurationService = get(),
            changeProfilePictureService = get(),
            passkeySettingsService = get(),
            androidCredentialService = get(),
            appVersionProvider = get()
        )
    }
}