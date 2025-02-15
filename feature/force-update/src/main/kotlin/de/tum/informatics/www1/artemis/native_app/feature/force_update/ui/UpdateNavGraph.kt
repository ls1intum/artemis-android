package de.tum.informatics.www1.artemis.native_app.feature.force_update.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.feature.force_update.UpdateViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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
        val viewModel = koinViewModel<UpdateViewModel> { parametersOf(onOpenPlayStore) }

        UpdateScreen(
            onDownloadClick = viewModel::onDownloadClick
        )
    }
}
