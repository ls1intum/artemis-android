package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val UPDATE_SCREEN_ROUTE = "update_screen"

fun NavController.navigateToUpdateScreen() {
    navigate(UPDATE_SCREEN_ROUTE) {
        popUpTo(graph.id) { inclusive = true } // Force user to see overlay
    }
}

fun NavGraphBuilder.updateNavGraph(
    onOpenPlayStore: () -> Unit,
) {
    composable(UPDATE_SCREEN_ROUTE) {

        UpdateScreen(
            onDownloadClick = onOpenPlayStore
        )
    }
}
