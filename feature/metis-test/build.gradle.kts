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
}
