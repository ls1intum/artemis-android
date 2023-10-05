plugins {
    id("artemis.android.library")
    id("artemis.android.flavor.library.instanceSelection")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.websocket.test"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:websocket"))
}
