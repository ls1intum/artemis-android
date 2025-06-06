plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("artemis.android.room")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.faq"
}

dependencies {
    implementation(project(":core:device"))
    testImplementation(project(":feature:faq-test"))
}