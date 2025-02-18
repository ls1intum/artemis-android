package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val UPDATE_SCREEN_ROUTE = "update_screen/{currentVersion}/{minVersion}"

fun NavController.navigateToUpdateScreen(currentVersion: String, minVersion: String) {
    navigate("update_screen/$currentVersion/$minVersion") {
        popUpTo(graph.startDestinationId) { inclusive = false }
    }
}


fun NavGraphBuilder.updateNavGraph(
    onOpenPlayStore: () -> Unit,
) {
    composable(
        route = UPDATE_SCREEN_ROUTE
    ) { backStackEntry ->
        val currentVersion = backStackEntry.arguments?.getString("currentVersion") ?: "Unknown"
        val minVersion = backStackEntry.arguments?.getString("minVersion") ?: "Unknown"

        UpdateScreen(
            onDownloadClick = onOpenPlayStore,
            currentVersion = currentVersion,
            minVersion = minVersion
        )

    }
}

