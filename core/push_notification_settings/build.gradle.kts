plugins {
    id("artemis.android.library")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.push_notification_settings"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))
    implementation(project(":core:ui"))

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.core)
}