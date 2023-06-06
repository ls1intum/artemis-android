package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.core.data.isSuccess
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicHintTextField
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.content.humanReadableName
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.conversationNavGraphBuilderExtension
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.isReplyEnabled
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.conversation.settings.overview.navigateToConversationSettingsScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.navigateToStandalonePostScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val ConversationDetailsRoute = "course/{courseId}/conversations/{conversationId}"

fun NavController.navigateToConversationDetailScreen(
    courseId: Long,
    conversationId: Long,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate("course/$courseId/conversations/$conversationId", builder)
}

fun NavGraphBuilder.conversationDetailScreen(
    navController: NavController,
    onNavigateBack: () -> Unit
) {
    conversationNavGraphBuilderExtension(
        route = ConversationDetailsRoute,
        deepLink = "artemis://courses/{courseId}/conversations/{conversationId}"
    ) { courseId, conversationId ->
        ConversationScreen(
            modifier = Modifier.fillMaxSize(),
            onNavigateBack = onNavigateBack,
            courseId = courseId,
            conversationId = conversationId,
            onNavigateToSettings = {
                navController.navigateToConversationSettingsScreen(courseId, conversationId) {}
            },
            onClickViewPost = { clientPostId ->
                navController.navigateToStandalonePostScreen(
                    clientPostId = clientPostId,
                    metisContext = MetisContext.Conversation(courseId, conversationId)
                ) {}
            }
        )
    }
}

@Composable
fun ConversationScreen(
    modifier: Modifier,
    courseId: Long,
    conversationId: Long,
    onNavigateBack: (() -> Unit)?,
    onNavigateToSettings: () -> Unit,
    onClickViewPost: (clientPostId: String) -> Unit
) {
    val metisContext = remember(courseId, conversationId) {
        MetisContext.Conversation(courseId, conversationId)
    }

    val viewModel = koinViewModel<MetisListViewModel> { parametersOf(metisContext) }

    LaunchedEffect(metisContext) {
        viewModel.updateMetisContext(metisContext)
    }

    val conversationDataState by viewModel.latestUpdatedConversation.collectAsState()

    var isSearchBarOpen by rememberSaveable(courseId, conversationId) { mutableStateOf(false) }
    val query by viewModel.query.collectAsState()

    val searchBarFocusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = modifier,
        topBar = {
            val title by remember(conversationDataState) {
                derivedStateOf {
                    conversationDataState.bind { it.humanReadableName }.orElse("Conversation")
                }
            }

            TopAppBar(
                title = {
                    if (isSearchBarOpen) {
                        LaunchedEffect(Unit) {
                            searchBarFocusRequester.requestFocus()
                        }

                        BasicHintTextField(
                            modifier = Modifier.focusRequester(searchBarFocusRequester),
                            value = query,
                            onValueChange = viewModel::updateQuery,
                            hint = "",
                            maxLines = 1,
                            hintStyle = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            modifier = Modifier.placeholder(visible = !conversationDataState.isSuccess),
                            text = title,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(
                            onClick = {
                                if (isSearchBarOpen) {
                                    isSearchBarOpen = false
                                    viewModel.updateQuery("")
                                } else {
                                    onNavigateBack()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    if (!isSearchBarOpen) {
                        IconButton(onClick = { isSearchBarOpen = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        }

                        IconButton(onClick = onNavigateToSettings) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                        }
                    }
                }
            )
        }
    ) { padding ->
        val isReplyEnabled = isReplyEnabled(conversationDataState = conversationDataState)

        MetisChatList(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            viewModel = viewModel,
            listContentPadding = PaddingValues(),
            onClickViewPost = onClickViewPost,
            isReplyEnabled = isReplyEnabled
        )
    }
}