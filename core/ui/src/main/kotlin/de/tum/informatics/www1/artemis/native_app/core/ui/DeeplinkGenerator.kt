package de.tum.informatics.www1.artemis.native_app.core.ui

import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkDslBuilder
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances

private val supportedUrls = ArtemisInstances.instances.map { it.host }

fun generateLinks(path: String, constructor: NavDeepLinkDslBuilder.() -> Unit = {}): List<NavDeepLink> {
    return supportedUrls.map { url ->
        navDeepLink {
            uriPattern = "$url/$path"
            constructor()
        }
    }
}