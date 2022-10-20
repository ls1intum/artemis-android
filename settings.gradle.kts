pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = "1.7.10"
    val nativeCoroutinesVersion = "0.12.6"

    plugins {
        id("com.android.application") version "7.2.2" apply false
        id("com.android.library") version "7.2.2" apply false
        kotlin("android") version kotlinVersion apply false
        kotlin("multiplatform") version kotlinVersion apply false

        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.parcelize") version kotlinVersion
        kotlin("native.cocoapods") version kotlinVersion apply false

        id("com.rickclephas.kmp.nativecoroutines") version nativeCoroutinesVersion apply false
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Artemis_Native_Client"
include(":androidApp")
include(":appCommon")