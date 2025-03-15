plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts"
}

dependencies {
    implementation(project(":core:device"))

    implementation(project(":feature:metis:shared"))
    implementation(project(":feature:metis:conversation:shared"))

    testImplementation(project(":feature:metis-test"))
}
