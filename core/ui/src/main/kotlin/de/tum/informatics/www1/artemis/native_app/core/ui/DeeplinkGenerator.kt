package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkDslBuilder
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances

private const val LegacyArtemisInstanceHost = "artemis.ase.in.tum.de"

private val supportedUrls = ArtemisInstances.instances.map { it.host } + LegacyArtemisInstanceHost + "artemis:/"

fun generateLinks(
    path: String,
    constructor: NavDeepLinkDslBuilder.() -> Unit = {}
): List<NavDeepLink> {
    return supportedUrls.map { url ->
        navDeepLink {
            uriPattern = "$url/$path"
            constructor()
        }
    }
}