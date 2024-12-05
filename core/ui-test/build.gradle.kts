plugins {
    id("artemis.android.library")
    id("artemis.android.library.compose")
    id("artemis.android.flavor.library.instanceSelection")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.ui.test"
}

dependencies {
    implementation(project(":core:ui"))

    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
