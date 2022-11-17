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

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.okhttp)

    //For the websockets
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.ktor)
    implementation(libs.krossbow.stomp.kxserialization.json)

    implementation(libs.koin.core)
}