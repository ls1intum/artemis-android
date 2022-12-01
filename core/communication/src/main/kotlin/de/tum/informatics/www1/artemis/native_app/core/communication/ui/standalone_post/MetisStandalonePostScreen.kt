package de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.*
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.MetisStandalonePostUi
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
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
            backStackEntry.arguments?.getString("viewType")
                ?: throw IllegalArgumentException("navType must not be null")
        )

        MetisStandalonePostScreen(
            clientPostId = clientPostId,
            viewType = viewType,
            onNavigateUp = onNavigateUp
        )
    }
}

@Composable
internal fun MetisStandalonePostScreen(
    clientPostId: String,
    viewType: ViewType,
    onNavigateUp: () -> Unit
) {
    val viewModel: MetisStandalonePostViewModel =
        koinViewModel(parameters = { parametersOf(clientPostId, true) })

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.standalone_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        MetisStandalonePostUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            viewModel = viewModel,
            viewType = viewType
        )
    }
}

internal enum class ViewType {
    REPLIES,
    WRITE_COMMENT
}