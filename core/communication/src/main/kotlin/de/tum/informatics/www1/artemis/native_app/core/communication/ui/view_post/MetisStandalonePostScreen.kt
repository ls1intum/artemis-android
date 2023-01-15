package de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisOutdatedBanner
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToStandalonePostScreen(
    clientPostId: String,
    metisContext: MetisContext,
    viewType: ViewType,
    builder: NavOptionsBuilder.() -> Unit
) {
    val metisContextAsString = Json.encodeToString(metisContext)

    navigate("metisStandalonePost/$clientPostId&$viewType&$metisContextAsString", builder)
}

fun NavGraphBuilder.standalonePostScreen(onNavigateUp: () -> Unit) {
    composable(
        "metisStandalonePost/{clientPostId}&{viewType}&{metisContext}",
        arguments = listOf(
            navArgument("clientPostId") {
                nullable = false
                type = NavType.StringType
            },
            navArgument("viewType") {
                nullable = false
                type = NavType.StringType
            },
            navArgument("metisContext") {
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

        val metisContext: MetisContext = Json.decodeFromString(
            backStackEntry.arguments?.getString("metisContext")
                ?: throw IllegalArgumentException("metisContext must not be null")
        )

        MetisStandalonePostScreen(
            clientPostId = clientPostId,
            viewType = viewType,
            onNavigateUp = onNavigateUp,
            metisContext = metisContext
        )
    }
}

@Composable
private fun MetisStandalonePostScreen(
    clientPostId: String,
    metisContext: MetisContext,
    viewType: ViewType,
    onNavigateUp: () -> Unit
) {
    val viewModel: MetisStandalonePostViewModel =
        koinViewModel(parameters = { parametersOf(clientPostId, metisContext, true) })

    val isDataOutdated by viewModel.isDataOutdated.collectAsState(initial = false)

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MetisOutdatedBanner(modifier = Modifier.fillMaxWidth(), isOutdated = isDataOutdated, requestRefresh = viewModel::requestWebsocketReload)

            MetisStandalonePostUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                viewModel = viewModel,
                viewType = viewType
            )
        }
    }
}

enum class ViewType {
    REPLIES,
    WRITE_COMMENT,
    POST
}