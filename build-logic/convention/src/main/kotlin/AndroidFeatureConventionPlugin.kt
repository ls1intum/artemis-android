@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import java.lang.Boolean
import kotlin.Suppress
import kotlin.apply
import kotlin.with

//Adapted from: https://github.com/android/nowinandroid/blob/bbc5460b624d67b64b5b5118f8a0e1763427e7e4/build-logic/convention/src/main/kotlin/AndroidFeatureConventionPlugin.kt
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("artemis.android.library")
            }

            extensions.configure<LibraryExtension> {
                configureInstanceSelectionFlavor(this)
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:datastore"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:common"))
                add("implementation", project(":core:websocket"))

//                add("testImplementation", kotlin("test"))
//                add("testImplementation", project(":core:testing"))
//                add("androidTestImplementation", kotlin("test"))
//                add("androidTestImplementation", project(":core:testing"))

//                add("implementation", libs.findLibrary("coil.kt").get())
//                add("implementation", libs.findLibrary("coil.kt.compose").get())

//                add("implementation", libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewModelCompose").get())

                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
                add("implementation", libs.findLibrary("koin.core").get())
                add("testImplementation", libs.findLibrary("koin.test").get())
                add("testImplementation", libs.findLibrary("koin.test.junit4").get())
                add("testImplementation", libs.findLibrary("koin.android.test").get())
                add("testImplementation", libs.findLibrary("robolectric").get())


                add("testImplementation", project(":core:common-test"))
                add("testImplementation", project(":core:data-test"))
                add("testImplementation", project(":core:core-test"))
            }

            afterEvaluate {
                tasks.withType(Test::class) {
                    testLogging.setEvents(listOf(TestLogEvent.FAILED))

                    testLogging.exceptionFormat = TestExceptionFormat.FULL
                    testLogging.showExceptions = true
                    testLogging.showCauses = true
                    testLogging.showStackTraces = true
                    testLogging.showStandardStreams = true

                    if (Boolean.getBoolean("skip.e2e")) {
                        useJUnit {
                            excludeCategories("de.tum.informatics.www1.artemis.native_app.core.common.test.EndToEndTest")
                        }
                    }

                    reports.junitXml.outputLocation.set(rootProject.rootDir.resolve("test-outputs/"))
                }
            }
        }
    }
}
