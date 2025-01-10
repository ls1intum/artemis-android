plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.courseview"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":feature:metis"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.placeholder.material)
    implementation(project(":core:device-test"))
    debugImplementation(project(":core:data-test"))

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))

    kover(project(":core:data"))
}