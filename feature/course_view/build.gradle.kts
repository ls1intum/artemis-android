plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.course_view"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":feature:metis"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.accompanist.placeholder.material)
}