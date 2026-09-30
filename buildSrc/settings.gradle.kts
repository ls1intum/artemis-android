pluginManagement {
    repositories {
        // Maven Central as mirrored by TUM AET: Maven Central answers the self-hosted E2E runners with
        // 429 Too Many Requests when a build has to fetch everything, e.g. after their Gradle cache was
        // cleared. What the mirror does not have is still found in the repositories after it.
        // Without this block the plugins would only come from the Gradle plugin portal, which sends the
        // requests for everything it has from Maven Central on to Maven Central.
        maven("https://reposilite.aet.cit.tum.de/releases")
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