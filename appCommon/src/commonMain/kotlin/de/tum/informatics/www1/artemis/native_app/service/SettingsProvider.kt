package de.tum.informatics.www1.artemis.native_app.service

import com.russhwolf.settings.coroutines.FlowSettings

interface SettingsProvider {

    fun createSettings(name: String): FlowSettings
}