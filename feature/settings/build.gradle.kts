plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.settings"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:push"))

    implementation(libs.accompanist.placeholder.material)
    implementation(libs.androidx.browser)

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
}