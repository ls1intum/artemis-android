plugins {
    id("artemis.android.library")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.core.common"
}

dependencies {
    api(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.datetime)

    implementation(libs.koin.core)

    api(libs.androidx.work.runtime.ktx)
    testImplementation(project(":core:common-test"))
}
