plugins {
    id("artemis.android.application")
    id("artemis.android.application.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
    id("com.google.android.gms.oss-licenses-plugin")
    id("io.sentry.android.gradle") version "3.3.0"
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.android"

    defaultConfig {
        applicationId = "de.tum.informatics.www1.artemis.native_app.android"
        versionCode = 5
        versionName = "0.3.0"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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
    implementation(project(":core:push_notification_settings"))

    implementation(project(":feature:course_registration"))
    implementation(project(":feature:course_view"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:login"))
    implementation(project(":feature:exercise_view"))
    implementation(project(":feature:lecture_view"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:quiz_participation"))

    implementation(libs.play.services.oss.licences)

    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.browser)

    implementation(libs.sentry.android)
    implementation(libs.sentry.compose.android)
}

sentry {
    autoInstallation.enabled.set(false)
}