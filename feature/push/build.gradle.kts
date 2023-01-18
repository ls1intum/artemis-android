plugins {
    id("artemis.android.feature")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.feature.push"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:device"))
    implementation(project(":core:datastore"))

    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.messaging)
    implementation(libs.koin.android.compat)
}