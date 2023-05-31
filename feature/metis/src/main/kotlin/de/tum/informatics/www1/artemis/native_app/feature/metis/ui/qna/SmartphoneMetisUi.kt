package de.tum.informatics.www1.artemis.native_app.feature.metis.ui.qna

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.feature.metis.model.MetisContext
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisListViewModel
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.post_list.MetisChatList
import de.tum.informatics.www1.artemis.native_app.feature.metis.ui.view_post.navigateToStandalonePostScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

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

            val navigateToStandalonePostScreen = { clientPostId: String ->
                navController.navigateToStandalonePostScreen(
                    clientPostId = clientPostId,
                    metisContext = metisContext
                ) {}
            }

            MetisChatList(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel,
                listContentPadding = PaddingValues(top = 8.dp, bottom = Spacings.FabContentBottomPadding)
            ) { clientPostId ->
                navigateToStandalonePostScreen(clientPostId)
            }
        }
    }
}