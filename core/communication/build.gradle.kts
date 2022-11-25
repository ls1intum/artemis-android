plugins {
    id("artemis.android.library")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.communication"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:websocket"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
}