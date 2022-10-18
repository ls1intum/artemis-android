plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    kotlin("plugin.parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "de.tum.informatics.www1.artemis.native_app.android"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.0"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.compose.material3:material3:1.0.0-rc01")

    implementation("io.insert-koin:koin-core:${koinVersion}")
    implementation("io.insert-koin:koin-core:${koinVersion}")
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-compose:$koinVersion")
    implementation("io.insert-koin:koin-android-compat:$koinVersion")

    implementation("androidx.compose.ui:ui:1.2.1")
    implementation("androidx.compose.ui:ui-tooling:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.1")
    implementation("androidx.compose.foundation:foundation:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.2.1")

    val accompanistVersion = "0.25.1"

    implementation("com.google.accompanist:accompanist-placeholder:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-navigation-animation:$accompanistVersion")

    implementation("androidx.activity:activity-compose:1.6.0")

    implementation("io.coil-kt:coil-compose:2.2.1")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    val lifecycle_version = "2.5.1"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.navigation:navigation-compose:2.5.2")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.4.0")

    implementation("com.auth0:java-jwt:4.1.0")
}