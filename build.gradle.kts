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

configurations.all {
    // Not used in project and created several security issues related to protobuf: https://github.com/advisories/GHSA-735f-pc8j-v9w8
    // As far as I understand, the affected dependency com.google.protobuf is added via https://android.googlesource.com/platform/frameworks/support/+/androidx-main/datastore/datastore-preferences-external-protobuf/build.gradle
    exclude(group = "androidx.datastore", module = "datastore-preferences-external-protobuf")
}