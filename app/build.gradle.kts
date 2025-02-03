import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
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
    id("io.sentry.android.gradle") version "5.0.0"
    id("artemis.android.room")
}

android {
    namespace = "de.tum.informatics.www1.artemis.native_app.android"

    val versionName = "1.2.0"
    val versionCode = 619
    // While we do not have a building pipeline set up, we default back to using manual version
    // codes, because otherwise we always have to re-install the app when checking out a branch with
    // less commits.
//    val versionCode = deriveVersionCodeFromGit()

    setProperty("archivesBaseName", "artemis-android-$versionName-$versionCode")

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

    defaultConfig applicationDefaultConfig@{
        applicationId = "de.tum.cit.aet.artemis"

        this@applicationDefaultConfig.versionCode = versionCode
        this@applicationDefaultConfig.versionName = versionName

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

            firebaseAppDistribution {
                artifactType = "APK"
                groups = "artemis-android-testers"
                serviceCredentialsFile = rootProject.projectDir.resolve("serviceCredentials.json").absolutePath
            }
        }
    }

    productFlavors {
        findByName(ProductFlavors.Dimensions.InstanceSelection.Flavors.FreeInstanceSelection)?.apply {
            versionNameSuffix =
                "-" + ProductFlavors.Dimensions.InstanceSelection.Flavors.FreeInstanceSelection

            isDefault = true
        }

        findByName(ProductFlavors.Dimensions.InstanceSelection.Flavors.Tum)?.apply {
            versionNameSuffix =
                "-" + ProductFlavors.Dimensions.InstanceSelection.Flavors.Tum
        }

        findByName(ProductFlavors.Dimensions.ReleaseType.Flavors.Beta)?.apply {
            versionNameSuffix =
                "-beta"
        }

        findByName(ProductFlavors.Dimensions.ReleaseType.Flavors.Production)?.apply {
            versionNameSuffix =
                "-prod"

            isDefault = true
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

    kover(project(":core:common"))
    kover(project(":core:data"))
    kover(project(":core:datastore"))
    kover(project(":core:model"))
    kover(project(":core:ui"))
    kover(project(":core:device"))
    kover(project(":core:websocket"))
    kover(project(":feature:course-registration"))
    kover(project(":feature:course-view"))
    kover(project(":feature:dashboard"))
    kover(project(":feature:login"))
    kover(project(":feature:exercise-view"))
    kover(project(":feature:lecture-view"))
    kover(project(":feature:settings"))
    kover(project(":feature:quiz"))
    kover(project(":feature:push"))
    kover(project(":feature:metis"))
    kover(project(":feature:core-modules-test"))

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
