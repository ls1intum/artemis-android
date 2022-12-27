pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "artemis_android"
include(":app")
include(":core:common")
include(":core:communication")
include(":core:data")
include(":core:datastore")
include(":core:model")
include(":core:ui")
include(":core:websocket")
include(":core:device")
include(":feature:course_registration")
include(":feature:dashboard")
include(":feature:course_view")
include(":feature:login")
include(":feature:exercise_view")
include(":feature:quiz_participation")
include(":feature:settings")