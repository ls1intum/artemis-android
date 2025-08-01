plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.coursenotifications"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))

    implementation(project(":feature:push"))
    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
}