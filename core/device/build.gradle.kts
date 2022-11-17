plugins {
    id("artemis.android.library")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.device"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
    implementation(libs.koin.android.compat)
}