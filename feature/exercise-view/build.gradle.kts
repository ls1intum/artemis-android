plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.exerciseview"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":feature:metis"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.toolbar.compose)

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
}