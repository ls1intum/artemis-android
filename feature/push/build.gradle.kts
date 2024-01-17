plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("artemis.android.room")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.push"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:device"))
    implementation(project(":core:ui"))
    implementation(project(":feature:metis"))
    implementation(project(":feature:metis:work"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.playServices)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.accompanist.permissions)

    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.messaging)
    implementation(libs.koin.android.compat)
    implementation(libs.koin.androidx.workmanager)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

    testImplementation(project(":feature:login"))
    testImplementation(project(":feature:login-test"))

    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
}