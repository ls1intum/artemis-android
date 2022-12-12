plugins {
    id("artemis.android.library")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.websocket"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.kotlinx.serialization.json)

    //For the websockets
    implementation(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.ktor)
    implementation(libs.krossbow.websocket.okhttp)
    implementation(libs.krossbow.stomp.kxserialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.koin.core)
}