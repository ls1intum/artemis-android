package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.create_standalone_post.navigateToCreateStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.filter_header.MetisFilterHeader
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisStandalonePostList
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post.MetisStandalonePostUi
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post.MetisStandalonePostViewModel
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post.ViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.view_post.navigateToStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisSortingStrategy
import de.tum.informatics.www1.artemis.native_app.core.ui.getWindowSizeClass
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Metis ui displayed on the right side of other content.
 */
@Composable
fun SideBarMetisUi(
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
            targetState = currentSelectedClientSidePostId
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

@Composable
fun SmartphoneMetisUi(
    modifier: Modifier,
    metisContext: MetisContext,
    navController: NavController,
    displayFab: Boolean = true
) {
    val viewModel: MetisListViewModel = koinViewModel { parametersOf(metisContext) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (displayFab) {
                FloatingActionButton(onClick = {
                    navController.navigateToCreateStandalonePostScreen(metisContext) {}
                }) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ViewModelMetisFilterHeaderImpl(
                modifier = Modifier.fillMaxWidth(),
                metisContext = metisContext,
                viewModel = viewModel
            )

            val navigateToStandalonePostScreen = { clientPostId: String, viewType: ViewType ->
                navController.navigateToStandalonePostScreen(
                    clientPostId = clientPostId,
                    viewType = viewType,
                    metisContext = metisContext
                ) {}
            }

            MetisStandalonePostList(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                listContentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                onClickViewPost = { clientPostId ->
                    navigateToStandalonePostScreen(clientPostId, ViewType.POST)
                },
                onClickViewReplies = { clientPostId ->
                    navigateToStandalonePostScreen(clientPostId, ViewType.REPLIES)
                },
                onClickCreatePost = null
            )
        }
    }
}

@Composable
private fun ViewModelMetisFilterHeaderImpl(
    modifier: Modifier,
    metisContext: MetisContext,
    viewModel: MetisListViewModel
) {
    val query: String = viewModel.query.collectAsState(initial = "").value

    val courseWideContext = viewModel.courseWideContext.collectAsState(initial = null).value
    val metisFilter = viewModel.filter.collectAsState(initial = emptyList()).value
    val sortingStrategy = viewModel
        .sortingStrategy
        .collectAsState(initial = MetisSortingStrategy.DATE_DESCENDING)
        .value

    MetisFilterHeader(
        modifier = modifier,
        context = metisContext,
        courseWideContext = courseWideContext,
        metisFilter = metisFilter,
        metisSortingStrategy = sortingStrategy,
        query = query,
        selectCourseWideContext = viewModel::updateCourseWideContext,
        onSelectFilter = { selectedFilter, isSelected ->
            if (isSelected) {
                viewModel.addMetisFilter(selectedFilter)
            } else {
                viewModel.removeMetisFilter(selectedFilter)
            }
        },
        onChangeMetisSortingStrategy = viewModel::updateSortingStrategy,
        onUpdateQuery = viewModel::updateQuery
    )
}

@Composable
internal fun ColumnScope.MetisOutdatedBanner(
    modifier: Modifier,
    isOutdated: Boolean,
    requestRefresh: () -> Unit
) {
    AnimatedVisibility(visible = isOutdated) {
        Box(
            modifier = modifier.then(
                Modifier.background(
                    MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(15)
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.metis_outdated_data_banner_text),
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                IconButton(onClick = requestRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

/**
 * If it is feasible to display metis on the side next to the main content of the screen.
 * @param parentWidth the maximum width of the parent in which the metis ui should be displayed
 * @param metisContentRatio the percentage of the screen width associated with metis
 */
@Composable
fun canDisplayMetisOnDisplaySide(
    windowSizeClass: WindowSizeClass = getWindowSizeClass(),
    parentWidth: Dp,
    metisContentRatio: Float
): Boolean {
    return (windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Expanded
            && (parentWidth * metisContentRatio) >= 300.dp)
}