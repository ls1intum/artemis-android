pluginManagement {
    includeBuild("../build-logic")

    repositories {
        google()
        // Maven Central as mirrored by TUM AET: Maven Central answers the self-hosted E2E runners with
        // 429 Too Many Requests when a build has to fetch everything, e.g. after their Gradle cache was
        // cleared. What the mirror does not have is still found in the repositories after it.
        maven("https://reposilite.aet.cit.tum.de/releases")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

include(":app")