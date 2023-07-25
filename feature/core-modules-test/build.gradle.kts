plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.coremodulestest"
}

dependencies {
    implementation(project(":core:data"))

    implementation(libs.kotlinx.datetime)

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))
    testImplementation(project(":core:core-test"))

    kover(project(":core:data"))
}

