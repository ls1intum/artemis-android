package de.tum.informatics.www1.artemis.native_app.feature.exerciseview.viewresult

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.ArtemisContext
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.collectArtemisContextAsState
import de.tum.informatics.www1.artemis.native_app.core.ui.common.EmptyDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.ArtemisTopAppBar
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.ExerciseViewModel
import de.tum.informatics.www1.artemis.native_app.feature.exerciseview.R

@Composable
internal fun ViewResultScreen(modifier: Modifier, viewModel: ExerciseViewModel, onCloseResult: () -> Unit) {
    val context by LocalArtemisContextProvider.current.collectArtemisContextAsState()
    // Captured once on entry. A different account/server/token must reopen from its own exercise.
    val session = remember { context as? ArtemisContext.LoggedIn }
    Scaffold(
        modifier = modifier,
        topBar = {
            ArtemisTopAppBar(
                title = { Text(stringResource(R.string.result_view_title)) },
                navigationIcon = { NavigationBackButton(onCloseResult) }
            )
        }
    ) { padding ->
        if (session == null || !sameReviewSession(session, context)) {
            ReviewMessage(Modifier.padding(padding), R.string.review_access_unavailable)
        } else {
            val exercise by viewModel.exerciseDataState.collectAsState()
            val result by viewModel.latestResultDataState.collectAsState()
            EmptyDataStateUi(exercise join result) { (loadedExercise, loadedResult) ->
                val snapshot = remember(loadedExercise.id) { loadedExercise }
                var selectedId by rememberSaveable(loadedExercise.id) { mutableStateOf(loadedResult?.id ?: selectReviewTarget(snapshot)?.result?.id) }
                val target = remember(snapshot, selectedId) { selectReviewTarget(snapshot, selectedId) }
                val targets = remember(snapshot) { reviewTargets(snapshot) }
                if (target == null) ReviewMessage(Modifier.padding(padding), R.string.review_no_result)
                else Column(Modifier.padding(padding).fillMaxSize()) {
                    if (targets.size > 1) {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) { Text(stringResource(R.string.review_select_assessment)) }
                            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                                targets.forEach { choice ->
                                    DropdownMenuItem(text = {
                                        Text(listOfNotNull(
                                            reviewDate(choice.result.completionDate?.toString()),
                                            reviewScore(choice.result.score, snapshot.maxPoints, snapshot.course?.accuracyOfScores?.toInt() ?: 1),
                                            if (choice.result.rated == false) stringResource(R.string.review_practice) else null
                                        ).joinToString(" · "))
                                    }, onClick = { selectedId = choice.result.id; expanded = false })
                                }
                            }
                        }
                    }
                    NativeResultReview(Modifier.weight(1f), snapshot, target, session)
                }
            }
        }
    }
}

internal fun sameReviewSession(session: ArtemisContext.LoggedIn, current: ArtemisContext): Boolean =
    current is ArtemisContext.LoggedIn && session.serverUrl == current.serverUrl &&
        session.authToken == current.authToken && session.loginName == current.loginName
