package commonConfiguration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import util.libs

internal fun Project.configureDependencyConstraints() {
    configurations.configureEach {
        // Unused Firebase component; see https://github.com/ls1intum/artemis-android/issues/339.
        exclude(group = "com.google.firebase", module = "firebase-measurement-connector")
    }

    dependencies {
        constraints {
            // Declare constraints on the dependency buckets so they also reach consumers of a
            // library. Constraints on a library's resolvable classpaths do not protect the app.
            listOf("implementation", "testImplementation").forEach { configuration ->
                add(configuration, libs.findLibrary("guava").get()) {
                    because("Fixes Guava deserialization vulnerability CVE-2026-102554")
                }
                add(configuration, libs.findLibrary("protobuf.java").get()) {
                    because("Keep the Protobuf runtime on a supported release with security fixes")
                }
                add(configuration, libs.findLibrary("google.play.services.basement").get()) {
                    because("Keep the Google Play services security constraint current")
                }
                add(configuration, libs.findLibrary("ktor.io").get()) {
                    because("Keep the patched Ktor modules aligned with the Ktor client")
                }
                add(configuration, libs.findLibrary("ktor.http.cio").get()) {
                    because("Keep the patched Ktor modules aligned with the Ktor client")
                }
                // Robolectric's Bouncy Castle dependency must not fall back to an old release.
                add(configuration, libs.findLibrary("bouncycastle.bcprov").get())
            }

            // Objenesis 2.6 was forced for an old Android test issue. Current Robolectric and
            // MockK can use the maintained release without downgrading every configuration.
            add("testImplementation", libs.findLibrary("objenesis").get())

            // Older Espresso releases cannot run against Android 17's InputManager APIs.
            add("testImplementation", libs.findLibrary("androidx.test.espresso.core").get())
        }
    }
}
