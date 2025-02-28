package de.tum.informatics.www1.artemis.native_app.core.common.app_version

data class AppVersion(
    val versionCode: Int,
    val fullVersionName: String,
) {

    val normalized: NormalizedAppVersion by lazy {
        NormalizedAppVersion.fromFullVersionName(fullVersionName)
    }
}
