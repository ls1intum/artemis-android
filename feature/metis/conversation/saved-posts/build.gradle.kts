plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
    id("kotlin-parcelize")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.saved_posts"
}

dependencies {
    implementation(project(":core:device"))

    implementation(project(":feature:metis:shared"))
}
