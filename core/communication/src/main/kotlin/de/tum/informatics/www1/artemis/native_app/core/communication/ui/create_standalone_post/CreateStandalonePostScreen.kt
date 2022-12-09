package de.tum.informatics.www1.artemis.native_app.core.communication.ui.create_standalone_post

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import org.koin.androidx.compose.getStateViewModel
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel

internal fun NavController.navigateToCreateStandalonePostScreen(
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("metisCreateStandalonePost", builder)
}

fun NavGraphBuilder.createStandalonePostScreen(
    onNavigateUp: () -> Unit,
    onCreatedPost: (clientSidePostId: String) -> Unit
) {
    composable(
        "metisCreateStandalonePost"
    ) {
        CreateStandalonePostScreen(
            onNavigateUp = onNavigateUp,
            onCreatedPost = onCreatedPost
        )
    }
}

@Composable
private fun CreateStandalonePostScreen(
    onNavigateUp: () -> Unit,
    onCreatedPost: (clientSidePostId: String) -> Unit
) {
    @Suppress("DEPRECATION")
    val viewModel: CreateStandalonePostViewModel = getStateViewModel()
    val canSave = viewModel.canCreatePost.collectAsState(initial = false).value

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
                    onClick = { viewModel.createPost() },
                    icon = { Icon(imageVector = Icons.Default.Create, contentDescription = null) },
                    text = { Text(text = stringResource(id = R.string.create_standalone_post_fab_create)) },
                    expanded = canSave
                )
            }
        }
    ) { padding ->
        CreateStandalonePostUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            viewModel = viewModel
        )
    }
}