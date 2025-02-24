package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
private data class UpdateScreenRoute(
    val currentVersion: String,
    val minVersion: String
)

fun NavController.navigateToUpdateScreen(currentVersion: String, minVersion: String) {
    navigate(UpdateScreenRoute(currentVersion, minVersion)) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
}

fun NavGraphBuilder.updateNavGraph(
    onOpenPlayStore: () -> Unit,
) {
    composable<UpdateScreenRoute> { backStackEntry ->
        val route: UpdateScreenRoute = backStackEntry.toRoute()

        UpdateScreen(
            onDownloadClick = onOpenPlayStore,
            currentVersion = route.currentVersion,
            minVersion = route.minVersion
        )
    }
}
