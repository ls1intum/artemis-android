plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.exercise_view"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":core:communication"))

    implementation(libs.kotlinx.datetime)
    implementation(libs.accompanist.placeholder.material)
    implementation(libs.accompanist.swiperefresh)
    implementation(libs.accompanist.flowlayout)

    implementation(libs.toolbar.compose)

}