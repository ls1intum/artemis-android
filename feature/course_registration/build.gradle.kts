plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.course_registration"
}

dependencies {
    implementation(libs.github.compose.markdown)
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
}