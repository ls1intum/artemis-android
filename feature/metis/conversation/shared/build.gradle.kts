plugins {
    id("artemis.android.feature")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared"
}

dependencies {
    implementation(project(":feature:metis:shared"))

    implementation(libs.placeholder.material)
}

