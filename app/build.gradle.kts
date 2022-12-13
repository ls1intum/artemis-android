plugins {
    id("artemis.android.application")
    id("artemis.android.application.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

val kotlinVersion = "1.7.0"
val coroutinesVersion = "1.6.4"

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.android"

    defaultConfig {
        applicationId = "de.tum.informatics.www1.artemis.native_app.android"
        versionCode = 2
        versionName = "0.1.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":core:device"))
    implementation(project(":core:websocket"))
    implementation(project(":core:communication"))

    implementation(project(":feature:course_registration"))
    implementation(project(":feature:course_view"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:login"))
    implementation(project(":feature:exercise_view"))

    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
}