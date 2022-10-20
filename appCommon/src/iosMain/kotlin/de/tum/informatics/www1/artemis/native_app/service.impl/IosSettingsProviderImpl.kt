package de.tum.informatics.www1.artemis.native_app.service.impl

import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import de.tum.informatics.www1.artemis.native_app.service.SettingsProvider
import platform.Foundation.NSUserDefaults

class IosSettingsProviderImpl : SettingsProvider {

    override fun createSettings(name: String): FlowSettings =
        AppleSettings(NSUserDefaults(name)).toFlowSettings()
}