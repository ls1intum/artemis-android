package de.tum.informatics.www1.artemis.native_app.core.communication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.list.MetisStandalonePostList
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.ViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post.navigateToStandalonePostScreen
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.MetisContext
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SmartphoneMetisUi(
    modifier: Modifier,
    metisContext: MetisContext,
    navController: NavController
) {
    val viewModel: MetisListViewModel = koinViewModel { parametersOf(metisContext) }

    MetisStandalonePostList(
        modifier = modifier,
        viewModel = viewModel,
        onClickReply = { clientPostId ->
            navController.navigateToStandalonePostScreen(
                clientPostId = clientPostId,
                viewType = ViewType.WRITE_COMMENT
            ) {}
        },
        onClickViewReplies = { clientPostId ->
            navController.navigateToStandalonePostScreen(
                clientPostId = clientPostId,
                viewType = ViewType.REPLIES
            ) {}
        }
    )
}
