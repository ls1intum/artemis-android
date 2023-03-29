package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.create_standalone_post

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.composable
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationResponse.Failure
import de.tum.informatics.www1.artemis.native_app.feature.metis.MetisModificationResponse.Response
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.MetisModificationFailureDialog
import kotlinx.coroutines.Job
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
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
    val viewModel: CreateStandalonePostViewModel = koinViewModel { parametersOf(metisContext) }
    val canSave = viewModel.canCreatePost.collectAsState(initial = false).value

    var modificationFailure: MetisModificationFailure? by remember { mutableStateOf(null) }

    var createPostJob: Job? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = createPostJob) {
        createPostJob?.let {
            it.join()
            createPostJob = null
        }
    }
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
            if (canSave || createPostJob != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        createPostJob?.cancel()

                        createPostJob = viewModel.createPost { response ->
                            when (response) {
                                is Failure -> modificationFailure = response.failure
                                is Response -> {
                                    onCreatedPost(response.data)
                                }
                            }
                        }
                    },
                    icon = {
                        if (createPostJob == null) {
                            Icon(imageVector = Icons.Default.Create, contentDescription = null)
                        } else {
                            CircularProgressIndicator()
                        }
                    },
                    text = { Text(text = stringResource(id = R.string.create_standalone_post_fab_create)) },
                    expanded = createPostJob == null
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