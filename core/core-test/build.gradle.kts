plugins {
    id("artemis.android.library")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.test"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))
    implementation(project(":core:device-test"))
    implementation(project(":core:data"))
    implementation(project(":core:data-test"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:websocket"))

    implementation(libs.koin.core)
}
