package de.tum.informatics.www1.artemis.native_app.feature.quiz.training

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.feature.quiz.R

/** Server league IDs descend from Master (1) to Bronze (5); 0 denotes an unknown league. */
@Composable
internal fun TrainingLeaderboardContext(entry: TrainingEntry) {
    val league =
        when (entry.selectedLeague) {
            1 -> R.string.training_league_master
            2 -> R.string.training_league_diamond
            3 -> R.string.training_league_gold
            4 -> R.string.training_league_silver
            5 -> R.string.training_league_bronze
            else -> null
        }
    league?.let { Text(stringResource(R.string.training_league, stringResource(it))) }
    entry.rank?.takeIf { it > 0 }?.let { Text(stringResource(R.string.training_own_rank, it)) }
}
