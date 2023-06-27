plugins {
    id("artemis.android.library")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.datetime)
}
