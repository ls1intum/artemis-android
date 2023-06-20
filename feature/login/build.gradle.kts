plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.login"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:push"))

    implementation(libs.androidx.dataStore.preferences)
    androidTestImplementation(libs.ktor.client.okhttp)
    implementation("io.ktor:ktor-client-cio:2.3.1")
}