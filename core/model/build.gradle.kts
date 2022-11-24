plugins {
    id("artemis.android.library")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.model"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.json)
}