pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = "1.7.10"
    val agpVersion = "7.3.1"

    plugins {
        id("com.android.application") version agpVersion apply false
        kotlin("android") version kotlinVersion apply false

        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.parcelize") version kotlinVersion
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