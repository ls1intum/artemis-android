package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import de.tum.informatics.www1.artemis.native_app.core.common.app_version.NormalizedAppVersion
import kotlinx.serialization.Serializable

@Serializable
private data class UpdateScreenRoute(
    val currentVersion: String,
    val minVersion: String,
    val recommendedVersion: String
)

fun NavController.navigateToUpdateScreen(
    currentVersion: NormalizedAppVersion,
    minVersion: NormalizedAppVersion,
    recommendedVersion: NormalizedAppVersion
) {
    navigate(
        UpdateScreenRoute(
            currentVersion = currentVersion.toString(),
            minVersion = minVersion.toString(),
            recommendedVersion = recommendedVersion.toString()
        )
    ) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
}

fun NavGraphBuilder.updateNavGraph(
    onOpenPlayStore: () -> Unit,
    onSkipUpdate: () -> Unit
) {
    composable<UpdateScreenRoute> { backStackEntry ->
        val route: UpdateScreenRoute = backStackEntry.toRoute()
        val currentVersion = NormalizedAppVersion(route.currentVersion)
        val minVersion = NormalizedAppVersion(route.minVersion)
        val recommendedVersion = NormalizedAppVersion(route.recommendedVersion)

        UpdateScreen(
            onDownloadClick = onOpenPlayStore,
            onSkipClick = onSkipUpdate,
            currentVersion = currentVersion,
            minVersion = minVersion,
            recommendedVersion = recommendedVersion
        )
    }
}
