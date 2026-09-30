@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import java.lang.Boolean as B


plugins {
    id("artemis.android.application")
    id("artemis.android.application.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.android.download_dependencies"

    defaultConfig {
        applicationId = "de.tum.informatics.www1.artemis.native_app.android.download_dependencies"
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}

repositories {
    google()
    // Maven Central as mirrored by TUM AET, see settings.gradle.kts
    maven("https://reposilite.aet.cit.tum.de/releases")
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    if (!B.getBoolean("skip.deps")) {
        val libs = project.libs
        libs.libraryAliases.map { libs.findLibrary(it) }.forEach {
            implementation(it.get())
        }
    }
}