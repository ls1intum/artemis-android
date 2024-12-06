plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.dashboard"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

    implementation(libs.accompanist.swiperefresh)
    debugImplementation(project(":core:ui-test"))
    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
}