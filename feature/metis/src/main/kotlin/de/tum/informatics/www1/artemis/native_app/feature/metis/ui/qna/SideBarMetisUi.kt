package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna.create_standalone_post.navigateToCreateStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisStandalonePostList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.MetisStandalonePostUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.MetisStandalonePostViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.ViewType
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Metis ui displayed on the right side of other content.
 */
@Composable
internal fun SideBarMetisUi(
    modifier: Modifier,
    metisContext: MetisContext,
    navController: NavController,
    title: @Composable () -> Unit
) {
    val viewModel: MetisListViewModel = koinViewModel { parametersOf(metisContext) }

    val selectedClientSidePostId: MutableState<String?> = rememberSaveable { mutableStateOf(null) }
    val currentSelectedClientSidePostId = selectedClientSidePostId.value

    val postListState = rememberLazyListState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ButtonDefaults.MinHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = currentSelectedClientSidePostId != null) {
                IconButton(onClick = { selectedClientSidePostId.value = null }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            }

            ProvideTextStyle(value = MaterialTheme.typography.titleLarge) {
                title()
            }
        }

        Crossfade(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            targetState = currentSelectedClientSidePostId,
            label = "Tablet post list to standalone post fade"
        ) { clientSidePostId ->
            if (clientSidePostId == null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ViewModelMetisFilterHeaderImpl(
                        modifier = Modifier.fillMaxWidth(),
                        metisContext = metisContext,
                        viewModel = viewModel
                    )

                    MetisStandalonePostList(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        viewModel = viewModel,
                        listContentPadding = PaddingValues(bottom = 8.dp),
                        state = postListState,
                        onClickViewPost = { clientPostId ->
                            selectedClientSidePostId.value = clientPostId
                        },
                        onClickViewReplies = { clientPostId ->
                            selectedClientSidePostId.value = clientPostId
                        },
                        onClickCreatePost = {
                            navController.navigateToCreateStandalonePostScreen(
                                metisContext
                            ) {}
                        }
                    )
                }
            } else {
                val standalonePostViewModel: MetisStandalonePostViewModel =
                    koinViewModel(parameters = {
                        parametersOf(
                            currentSelectedClientSidePostId,
                            metisContext,
                            false
                        )
                    })

                MetisStandalonePostUi(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    viewModel = standalonePostViewModel,
                    viewType = ViewType.POST
                )
            }
        }
    }
}