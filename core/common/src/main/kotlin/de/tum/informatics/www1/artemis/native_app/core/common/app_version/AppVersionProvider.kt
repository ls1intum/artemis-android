package de.tum.informatics.www1.artemis.native_app.core.common.app_version

interface AppVersionProvider {
    val appVersion: AppVersion
}

class AppVersionProviderImpl : AppVersionProvider {
    override var appVersion: AppVersion = AppVersion.UNKNOWN
}