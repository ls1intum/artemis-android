buildscript {
    repositories {
        google()
        // Maven Central as mirrored by TUM AET, see settings.gradle.kts
        maven("https://reposilite.aet.cit.tum.de/releases")
        mavenCentral()

        maven("https://jitpack.io")
    }
    dependencies {
        classpath(libs.oss.licenses.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.firebase.appdistribution) apply false
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover) apply true
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}