plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("artemis.android.room")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.conversation"
}

dependencies {
    implementation(project(":core:device"))
    implementation(project(":feature:faq"))
    implementation(project(":feature:metis:conversation:emoji-picker"))
    testImplementation(project(":core:ui-test"))

    implementation(project(":feature:metis:shared"))
    api(project(":feature:metis:conversation:shared"))
    api(project(":feature:metis:conversation:saved-posts"))
    testImplementation(project(":feature:metis-test"))

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.placeholder.material)

    implementation(libs.koin.androidx.workmanager)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.dataStore.preferences)
    implementation(libs.androidx.paging.common)
    
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
}