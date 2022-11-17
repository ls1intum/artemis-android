import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("artemis.android.application")
    id("artemis.android.application.compose")
}

 val kotlinVersion = "1.7.0"
 val coroutinesVersion = "1.6.4"

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.android"

    defaultConfig {
        applicationId = "de.tum.informatics.www1.artemis.native_app.android"
        versionCode = 1
        versionName = "1.0"
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

    implementation(project(":feature:course_registration"))
    implementation(project(":feature:course_view"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:login"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
}