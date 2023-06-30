plugins {
    id("artemis.android.library")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.device.test"
}

dependencies {
    implementation(project(":core:device"))

    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
