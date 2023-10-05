buildscript {
    repositories {
        google()
        mavenCentral()

        maven("https://jitpack.io")
    }
    dependencies {
        classpath(libs.oss.licenses.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.google.firebase.appdistribution) apply false
    alias(libs.plugins.github.benManes.versions) apply true
    alias(libs.plugins.kover) apply true
    alias(libs.plugins.google.ksp) apply false
    alias(libs.plugins.cashapp.paparazzi) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}