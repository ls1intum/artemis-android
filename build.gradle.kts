buildscript {
    repositories {
        google()
        mavenCentral()

        maven("https://jitpack.io")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.google.firebase.appdistribution) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}