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
    implementation(project(":feature:login"))

    implementation(libs.krop.core)
    implementation(libs.krop.ui)
    implementation(libs.placeholder.material)
    implementation(libs.androidx.browser)
    implementation(project(":feature:metis:shared"))

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
}