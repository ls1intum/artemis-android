package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.post.post_actions

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PostActionBar(
    modifier: Modifier,
    postActions: PostActions,
    repliesCount: Int
) {
    Row {
        Text(text = repliesCount.toString())
        Spacer(modifier = Modifier.weight(1f))
        ActionBar()
    }
}

@Composable
private fun ActionBar() {

}