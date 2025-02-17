plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.forceupdate"
}

dependencies {
    implementation(project(":core:device"))

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

}
