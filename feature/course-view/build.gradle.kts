plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.courseview"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":feature:metis"))
    implementation(project(":feature:lecture-view"))
    implementation(project(":feature:metis:conversation:emoji-picker"))
    implementation(project(":feature:faq"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.placeholder.material)
    implementation(project(":core:device-test"))
    debugImplementation(project(":core:data-test"))

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
    testImplementation(project(":feature:metis-test"))

    kover(project(":core:data"))
}