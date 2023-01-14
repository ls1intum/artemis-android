package de.tum.informatics.www1.artemis.native_app.core.communication.ui.create_standalone_post

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationResponse
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisModificationFailureDialog
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.getStateViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToCreateStandalonePostScreen(
    metisContext: MetisContext,
    builder: NavOptionsBuilder.() -> Unit
) {
    val metisContextAsString = Json.encodeToString(metisContext)
    navigate("metisCreateStandalonePost/$metisContextAsString", builder)
}

fun NavGraphBuilder.createStandalonePostScreen(
    onNavigateUp: () -> Unit,
    onCreatedPost: (clientSidePostId: String, metisContext: MetisContext) -> Unit
) {
    composable(
        "metisCreateStandalonePost/{metisContext}",
        arguments = listOf(
            navArgument(
                "metisContext"
            ) {
                type = NavType.StringType
                nullable = false
            }
        )
    ) {
        val metisContextAsString = it.arguments?.getString("metisContext")
        checkNotNull(metisContextAsString)

        val metisContext: MetisContext = Json.decodeFromString(metisContextAsString)

        CreateStandalonePostScreen(
            metisContext = metisContext,
            onNavigateUp = onNavigateUp,
            onCreatedPost = { clientSidePostId -> onCreatedPost(clientSidePostId, metisContext) }
        )
    }
}

@Composable
private fun CreateStandalonePostScreen(
    metisContext: MetisContext,
    onNavigateUp: () -> Unit,
    onCreatedPost: (clientSidePostId: String) -> Unit
) {
    @Suppress("DEPRECATION")
    val viewModel: CreateStandalonePostViewModel = getStateViewModel { parametersOf(metisContext) }
    val canSave = viewModel.canCreatePost.collectAsState(initial = false).value

    var modificationFailure: MetisModificationFailure? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.create_standalone_post_title)
                    )
                }
            )
        },
        floatingActionButton = {
            if (canSave) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.createPost { response ->
                            when(response) {
                                is MetisModificationResponse.Failure -> modificationFailure = response.failure
                                is MetisModificationResponse.Response -> {
                                    onCreatedPost(response.data)
                                }
                            }
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Create, contentDescription = null) },
                    text = { Text(text = stringResource(id = R.string.create_standalone_post_fab_create)) },
                    expanded = true
                )
            }
        }
    ) { padding ->
        CreateStandalonePostUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp),
            viewModel = viewModel
        )

        MetisModificationFailureDialog(metisModificationFailure = modificationFailure) {
            modificationFailure = null
        }
    }
}