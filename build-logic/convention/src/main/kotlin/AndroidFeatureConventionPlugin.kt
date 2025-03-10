@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import util.libs

//Adapted from: https://github.com/android/nowinandroid/blob/bbc5460b624d67b64b5b5118f8a0e1763427e7e4/build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("artemis.android.library")
                apply("org.gradle.jacoco")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            extensions.configure<LibraryExtension> {
                configureInstanceSelectionFlavors(this)

                buildTypes {
                    all {
                        enableUnitTestCoverage = true
                    }
                }
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:datastore"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:websocket"))

                add("implementation", libs.findLibrary("androidx.lifecycle.viewModelCompose").get())

                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
                add("implementation", libs.findLibrary("koin.core").get())

                add("testImplementation", project(":core:common-test"))
                add("testImplementation", project(":core:data-test"))
                add("testImplementation", project(":core:core-test"))
            }
        }
    }
}
