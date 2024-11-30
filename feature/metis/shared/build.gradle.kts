plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("artemis.android.room")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.shared"
}
dependencies {
    implementation(project(":core:device"))
    testImplementation(project(":feature:metis-test"))
}
