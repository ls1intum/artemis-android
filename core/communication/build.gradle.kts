plugins {
    id("artemis.android.library")
    id("artemis.android.library.compose")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.communication"
}

dependencies {
    api(project(":core:ui"))
}