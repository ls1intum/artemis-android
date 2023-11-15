plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("artemis.android.room")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:websocket"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))

    api(project(":feature:metis:shared"))
    api(project(":feature:metis:code-of-conduct"))
    api(project(":feature:metis:conversation"))
    api(project(":feature:metis:manage-conversations"))

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

    kover(project(":feature:metis:shared"))
    kover(project(":feature:metis:conversation"))
    kover(project(":feature:metis:manage-conversations"))
}
