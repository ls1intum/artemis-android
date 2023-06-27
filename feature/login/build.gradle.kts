@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import java.lang.Boolean as B

plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.login"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:push"))

    implementation(libs.androidx.dataStore.preferences)
    testImplementation(project(":core:data-test"))
}

project.afterEvaluate {
    tasks.getByName("test").onlyIf { !B.getBoolean("skip.tests") }
    tasks.getByName("testDebugUnitTest").onlyIf { !B.getBoolean("skip.tests") }
    tasks.getByName("testReleaseUnitTest").onlyIf { !B.getBoolean("skip.tests") }

    tasks.withType(Test::class) {
        testLogging.showStandardStreams = true
    }
}
