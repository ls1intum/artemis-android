plugins {
    id("artemis.android.library")
    id("artemis.android.flavor.library.instanceSelection")
    kotlin("plugin.serialization")
    id("artemis.android.room")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.datastore"

    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.security.crypto)

    implementation(libs.ktor.client.core)
    implementation(libs.koin.core)
    implementation(libs.koin.android.compat)

    implementation(libs.androidx.room.paging)

    implementation(libs.kotlinx.datetime)

    implementation(libs.auth0.java.jwt)
}