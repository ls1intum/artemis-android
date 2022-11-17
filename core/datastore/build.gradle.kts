plugins {
    id("artemis.android.library")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.datastore"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

    implementation(libs.ktor.client.core)
    implementation(libs.koin.core)
    implementation(libs.koin.android.compat)

    implementation("com.auth0:java-jwt:4.1.0")
}