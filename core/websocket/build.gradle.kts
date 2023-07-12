plugins {
    id("artemis.android.library")
    kotlin("plugin.serialization")
    id("artemis.android.flavor.library.instanceSelection")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.websocket"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.kotlinx.serialization.json)

    //For the websockets
    api(libs.krossbow.stomp.core)
    implementation(libs.krossbow.websocket.ktor)
    implementation(libs.krossbow.websocket.okhttp)
    api(libs.krossbow.stomp.kxserialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.koin.core)
}