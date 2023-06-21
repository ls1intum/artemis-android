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
include(":core:data")
include(":core:data-test")
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
include(":feature:lecture_view")
include(":feature:quiz")
include(":feature:settings")
include(":feature:push")
include(":feature:metis")