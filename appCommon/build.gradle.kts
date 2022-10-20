plugins {
    kotlin("multiplatform")
    id("com.android.library")

    kotlin("plugin.serialization")
    kotlin("plugin.parcelize")

    id("com.rickclephas.kmp.nativecoroutines")
    id("dev.icerock.moko.kswift") version "0.6.0"
}

kotlin {
    android()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries {
            framework {
                baseName = "appCommon"

                export("com.arkivanov.essenty:lifecycle:$essentyVersion")
                export("com.arkivanov.decompose:decompose:$decomposeVersion")
            }
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
        }

        val commonMain by getting {
            dependencies {
                api("io.insert-koin:koin-core:${koinVersion}")
                api("com.arkivanov.decompose:decompose:$decomposeVersion")
                api("com.arkivanov.essenty:lifecycle:$essentyVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-serialization:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
                implementation("com.russhwolf:multiplatform-settings:0.9")
                implementation("com.russhwolf:multiplatform-settings-coroutines:0.9")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
                implementation("com.russhwolf:multiplatform-settings-datastore:0.9")
            }
        }
        val androidTest by getting

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "de.tum.in.www1.artemis.native_app"
    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }
}

kswift {
    install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature)
}