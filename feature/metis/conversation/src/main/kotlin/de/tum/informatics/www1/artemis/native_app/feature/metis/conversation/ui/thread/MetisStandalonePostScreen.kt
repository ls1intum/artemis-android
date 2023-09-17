package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.thread

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.shared.MetisOutdatedBanner
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToStandalonePostScreen(
    clientPostId: String,
    metisContext: MetisContext,
    builder: NavOptionsBuilder.() -> Unit
) {
    val metisContextAsString = Json.encodeToString(metisContext)

    navigate("metisStandalonePost/$clientPostId&$metisContextAsString", builder)
}

fun NavGraphBuilder.standalonePostScreen(onNavigateUp: () -> Unit) {
    composable(
        route = "metisStandalonePost/{clientPostId}&{metisContext}",
        arguments = listOf(
            navArgument("clientPostId") {
                nullable = true
                type = NavType.StringType
            },
            navArgument("serverPostId") {
                nullable = true
                type = NavType.StringType
            },
            navArgument("metisContext") {
                nullable = true
                type = NavType.StringType
            },
            navArgument("courseId") {
                nullable = true
                type = NavType.StringType
            },
            navArgument("exerciseId") {
                nullable = true
                type = NavType.StringType
            },
            navArgument("lectureId") {
                nullable = true
                type = NavType.StringType
            }
        ),
        deepLinks = listOf(
            navDeepLink {
                uriPattern =
                    "artemis://metis_standalone_post/{serverPostId}/{courseId}/{exerciseId}/{lectureId}/{conversationId}"
            }
        )
    ) { backStackEntry ->
        val clientPostId =
            backStackEntry.arguments?.getString("clientPostId")

        val serverPostId =
            backStackEntry.arguments?.getString("serverPostId")?.toLongOrNull()

        check(clientPostId != null || serverPostId != null)

        val postId = if (clientPostId != null) {
            StandalonePostId.ClientSideId(clientPostId)
        } else {
            // !! checked by check before
            StandalonePostId.ServerSideId(serverPostId!!)
        }

        val metisContextArg = backStackEntry.arguments?.getString("metisContext")
        val metisContext: MetisContext = if (metisContextArg != null) {
            Json.decodeFromString(metisContextArg)
        } else {
            val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull()
            if (courseId != null) {
                val exerciseId = backStackEntry.arguments?.getString("exerciseId")?.toLongOrNull()
                val lectureId = backStackEntry.arguments?.getString("lectureId")?.toLongOrNull()
                val conversationId =
                    backStackEntry.arguments?.getString("conversationId")?.toLongOrNull()

                when {
                    exerciseId != null -> MetisContext.Exercise(courseId, exerciseId)
                    lectureId != null -> MetisContext.Lecture(courseId, lectureId)
                    conversationId != null -> MetisContext.Conversation(courseId, conversationId)
                    else -> MetisContext.Course(courseId)
                }
            } else null
        } ?: return@composable // Invalid input, not sure how to handle therefore display nothing

        MetisStandalonePostScreen(
            modifier = Modifier.fillMaxSize(),
            standalonePostId = postId,
            onNavigateUp = onNavigateUp,
            metisContext = metisContext
        )
    }
}

/**
 * Display the post and its replied. If metis may be outdated, a banner will be displayed to the user.
 */
@Composable
fun MetisStandalonePostScreen(
    modifier: Modifier,
    standalonePostId: StandalonePostId,
    metisContext: MetisContext,
    onNavigateUp: () -> Unit
) {
    val viewModel: MetisThreadViewModel =
        getViewModel(parameters = { parametersOf(standalonePostId, metisContext, true) })

    LaunchedEffect(metisContext) {
        viewModel.updateMetisContext(metisContext)
    }

    LaunchedEffect(standalonePostId) {
        viewModel.updatePostId(standalonePostId)
    }

    val isDataOutdated by viewModel.isDataOutdated.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.standalone_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::requestReload) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
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
            MetisOutdatedBanner(
                modifier = Modifier.fillMaxWidth(),
                isOutdated = isDataOutdated,
                requestRefresh = viewModel::requestReload
            )

            MetisThreadUi(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                viewModel = viewModel
            )
        }
    }
}
