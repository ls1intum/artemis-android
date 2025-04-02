plugins {
    id("artemis.android.library")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.common.test"
}
dependencies {
    implementation(project(":core:common"))
}
