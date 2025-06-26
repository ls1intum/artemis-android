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
include(":core:common-test")
include(":core:data")
include(":core:data-test")
include(":core:datastore")
include(":core:model")
include(":core:ui")
include(":core:ui-test")
include(":core:websocket")
include(":core:device")
include(":core:device-test")
include(":core:core-test")
include(":feature:course-notifications")
include(":feature:course-registration")
include(":feature:dashboard")
include(":feature:course-view")
include(":feature:login")
include(":feature:login-test")
include(":feature:exercise-view")
include(":feature:lecture-view")
include(":feature:quiz")
include(":feature:settings")
include(":feature:push")
include(":feature:faq")
include(":feature:faq-test")
include(":feature:force-update")

include(":feature:metis")
include(":feature:metis:shared")
include(":feature:metis:code-of-conduct")
include(":feature:metis:conversation")
include(":feature:metis:conversation:emoji-picker")
include(":feature:metis:conversation:saved-posts")
include(":feature:metis:conversation:shared")
include(":feature:metis:manage-conversations")
include(":feature:metis-test")

include(":feature:core-modules-test")
