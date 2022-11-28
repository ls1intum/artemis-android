plugins {
    id("artemis.android.library")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}