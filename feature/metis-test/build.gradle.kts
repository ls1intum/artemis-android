plugins {
    id("artemis.android.feature")
    id("artemis.android.room")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.metistest"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
    implementation(project(":feature:metis"))

    api(project(":feature:login"))
    api(project(":feature:login-test"))

    api(project(":core:common-test"))
    api(project(":core:data-test"))
    api(project(":core:core-test"))

    api(libs.koin.test.junit4)
    api(libs.robolectric)
    api(libs.koin.android.test)
}
