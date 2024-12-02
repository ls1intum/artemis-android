plugins {
    id("artemis.android.library")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.ui.test"
}

dependencies {
    implementation(project(path = ":core:ui", configuration = "productionTumDebugApiElements"))

    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
