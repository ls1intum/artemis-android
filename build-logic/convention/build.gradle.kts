/*
 * Heavily borrowed from https://github.com/android/nowinandroid/blob/main/build-logic/convention/build.gradle.kts
 */

plugins {
    `kotlin-dsl`
}

group = "de.tum.informatics.www1.artemis.native_app.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.kotlin.kover)
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "artemis.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "artemis.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "artemis.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "artemis.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "artemis.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidRoom") {
            id = "artemis.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidLibraryInstanceSelectionFlavor") {
            id = "artemis.android.flavor.library.instanceSelection"
            implementationClass = "AndroidLibraryInstanceSelectionFlavorConventionPlugin"
        }
    }
}