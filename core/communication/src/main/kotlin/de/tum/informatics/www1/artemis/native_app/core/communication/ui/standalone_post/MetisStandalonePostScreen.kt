package de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.MetisStandalonePostUi
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun NavController.navigateToStandalonePostScreen(
    clientPostId: String,
    viewType: ViewType,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("metisStandalonePost/$clientPostId&$viewType", builder)
}

fun NavGraphBuilder.standalonePostScreen(onNavigateUp: () -> Unit) {
    composable(
        "metisStandalonePost/{clientPostId}&{viewType}",
        arguments = listOf(
            navArgument("clientPostId") {
                nullable = false
                type = NavType.StringType
            },
            navArgument("viewType") {
                nullable = false
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val clientPostId =
            backStackEntry.arguments?.getString("clientPostId")
        checkNotNull(clientPostId)

        val viewType = ViewType.valueOf(
            backStackEntry.arguments?.getString("clientPostId")
                ?: throw IllegalArgumentException("navType must not be null")
        )

        MetisStandalonePostScreen(clientPostId = clientPostId, viewType = viewType)
    }
}

@Composable
internal fun MetisStandalonePostScreen(clientPostId: String, viewType: ViewType) {
    val viewModel: MetisStandalonePostViewModel =
        koinViewModel(parameters = { parametersOf(clientPostId) })

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "")
                }
            )
        }
    ) { padding ->

    }
    MetisStandalonePostUi(
        modifier = Modifier.fillMaxSize(),
        clientPostId = clientPostId,
        viewType = viewType
    )
}

internal enum class ViewType {
    REPLIES,
    WRITE_COMMENT
}