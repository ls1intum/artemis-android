plugins {
    id("artemis.android.feature")
    id("artemis.android.room")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.work"
}

dependencies {
    implementation(project(":core:device"))

    implementation(libs.koin.androidx.workmanager)
    implementation(libs.androidx.work.runtime.ktx)
}