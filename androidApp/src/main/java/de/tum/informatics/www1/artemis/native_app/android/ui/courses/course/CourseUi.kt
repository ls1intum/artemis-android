package de.tum.informatics.www1.artemis.native_app.android.ui.courses.course

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.google.accompanist.placeholder.material.placeholder
import de.tum.informatics.www1.artemis.native_app.android.R
import de.tum.informatics.www1.artemis.native_app.android.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.android.util.DataState
import de.tum.informatics.www1.artemis.native_app.android.util.isSuccess
import org.koin.androidx.compose.getViewModel

@Composable
fun CourseUi(
    modifier: Modifier,
    viewModel: CourseViewModel = getViewModel(),
    onNavigateBack: () -> Unit
) {
    val courseDataState by viewModel.course.collectAsState(initial = DataState.Loading())

    val topAppBarState = rememberTopAppBarState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        topAppBarState
    )

    Scaffold(
        modifier = modifier.then(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.placeholder(visible = !courseDataState.isSuccess),
                        text = courseDataState.bind { it.title }.orElse("Placeholder course title")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            var selectedTabIndex by remember { mutableStateOf(0) }

            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text(text = stringResource(id = R.string.course_ui_tab_exercises)) }
                )
            }
        }
        BasicDataStateUi(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            dataState = courseDataState,
            loadingText = "",
            failureText = "",
            suspendedText = "",
            retryButtonText = "",
            onClickRetry = { /*TODO*/ }
        ) { data ->
            Text(text = data.title)
        }
    }
}