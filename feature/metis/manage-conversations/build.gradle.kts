plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations"
}

dependencies {
    implementation(project(":core:device"))

    implementation(project(":feature:metis:shared"))
    implementation(project(":feature:metis:code-of-conduct"))

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    testImplementation(project(":feature:metis-test"))
}

