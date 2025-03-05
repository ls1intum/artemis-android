package de.tum.informatics.www1.artemis.native_app.core.ui.deeplinks

import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkDslBuilder
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.core.datastore.defaults.ArtemisInstances


abstract class ArtemisDeeplink {
    abstract val path: String
    /**
     * The type of the deeplink. This determines on which hosts the deeplink is supported.
     */
    abstract val type: Type

    fun generateLinks(
        constructor: NavDeepLinkDslBuilder.() -> Unit = {}
    ): List<NavDeepLink> {
        val supportedHosts = when (type) {
            Type.ONLY_IN_APP -> listOf(IN_APP_HOST)
            Type.IN_APP_AND_WEB -> WEB_HOSTS + IN_APP_HOST
        }

        return supportedHosts.map { host ->
            navDeepLink {
                uriPattern = "$host/$path"
                constructor()
            }
        }
    }

    enum class Type {
        ONLY_IN_APP,
        IN_APP_AND_WEB,
    }

    companion object {
        const val IN_APP_HOST = "artemis:/"        // Defined in intent filters in AndroidManifest.xml
        val WEB_HOSTS = ArtemisInstances.instances.map { it.host } + ArtemisInstances.LegacyTumArtemis.host
    }
}