@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import commonConfiguration.configureJacoco
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

//Adapted from: https://github.com/android/nowinandroid/blob/bbc5460b624d67b64b5b5118f8a0e1763427e7e4/build-logic/convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("org.gradle.jacoco")
                apply("org.jetbrains.kotlinx.kover")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 33
            }

            configurations.configureEach {
                resolutionStrategy {
                    // Temporary workaround for https://issuetracker.google.com/174733673
                    force("org.objenesis:objenesis:2.6")
                }
            }

            configureJacoco(extensions.getByType<LibraryAndroidComponentsExtension>())

            dependencies {
                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())

                add("testImplementation", libs.findLibrary("koin.test").get())
                add("testImplementation", libs.findLibrary("koin.test.junit4").get())
                add("testImplementation", libs.findLibrary("koin.android.test").get())
                add("testImplementation", libs.findLibrary("robolectric").get())
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

                    if (Boolean.getBoolean("skip.unit-tests")) {
                        useJUnit {
                            excludeCategories("de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest")
                        }
                    }

                    reports.junitXml.required.set(true)
                    reports.junitXml.outputLocation.set(rootProject.rootDir.resolve("test-outputs/${project.name}/$name/"))
                }
            }
        }
    }
}
