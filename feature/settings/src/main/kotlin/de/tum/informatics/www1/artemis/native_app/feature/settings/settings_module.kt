package de.tum.informatics.www1.artemis.native_app.feature.settings

import de.tum.informatics.www1.artemis.native_app.feature.settings.ui.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            accountService = get(),
            accountDataService = get(),
            networkStatusProvider = get(),
            pushNotificationJobService = get(),
            pushNotificationConfigurationService = get(),
            appVersionProvider = get()
        )
    }
}