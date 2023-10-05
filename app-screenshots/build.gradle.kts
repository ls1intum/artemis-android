plugins {
    id("artemis.android.feature")
    id("app.cash.paparazzi")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.screenshots"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:device"))
    implementation(project(":core:websocket"))

    implementation(project(":feature:course-registration"))
    implementation(project(":feature:course-view"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:login"))
    implementation(project(":feature:exercise-view"))
    implementation(project(":feature:lecture-view"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:push"))
    implementation(project(":feature:metis"))

    api(project(":core:common-test"))
    api(project(":core:data-test"))
    api(project(":core:core-test"))
    api(project(":core:datastore-test"))
    api(project(":core:websocket-test"))


    api(libs.koin.test.junit4)
    api(libs.robolectric)
    api(libs.koin.android.test)
}
