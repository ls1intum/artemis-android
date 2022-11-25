plugins {
    id("artemis.android.library")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:device"))

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.kotlinx.serialization.json)

    api(libs.ktor.client.core)
    api(libs.ktor.client.serialization)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.koin.core)
    implementation(libs.kotlinx.datetime)
}