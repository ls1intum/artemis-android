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

sourceSets {
    create("endToEndTests") {
        java.srcDirs("src/endToEndTests/kotlin")
    }
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:push"))

    implementation(libs.androidx.dataStore.preferences)
}


project.afterEvaluate {
    tasks.withType(Test::class) {
        testLogging.showStandardStreams = true

        if (B.getBoolean("skip.e2e")) {
            useJUnit {
                excludeCategories("de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest")
            }
        }
    }
}
