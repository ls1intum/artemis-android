import java.io.FileInputStream
import java.util.Properties

// https://developer.android.com/studio/publish/app-signing#secure-shared-keystore
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties: Properties? = if (keystorePropertiesFile.exists()) Properties().apply {
    load(FileInputStream(keystorePropertiesFile))
} else null

plugins {
    id("artemis.android.application")
    id("artemis.android.application.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
    id("com.google.android.gms.oss-licenses-plugin")
    id("io.sentry.android.gradle") version "3.9.0"
    id("artemis.android.room")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.android"

    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "de.tum.informatics.www1.artemis.native_app.android"
        versionCode = System.getenv("bamboo.repository.revision.number").toIntOrNull() ?: deriveVersionCodeFromGit()
        versionName = "0.7.2"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
        if (keystoreProperties != null) {
            getByName("release") {
                isMinifyEnabled = false
                signingConfig = signingConfigs.getByName("release")
            }
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
    implementation(project(":feature:exercise_view"))
    implementation(project(":feature:lecture_view"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:quiz"))
    implementation(project(":feature:push"))
    implementation(project(":feature:metis"))

    implementation(libs.play.services.oss.licences)

    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.browser)

    implementation(libs.sentry.android)
    implementation(libs.sentry.compose.android)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.androidx.workmanager)
}

sentry {
    autoInstallation.enabled.set(false)
}

/**
 * The version code is the number of commits in the current branch.
 */
fun deriveVersionCodeFromGit(): Int {
    return providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim().toInt()
}