package de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.AnswerPostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getEmojiForEmojiId
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post

@Composable
internal fun MetisStandalonePostUi(
    modifier: Modifier,
    viewModel: MetisStandalonePostViewModel,
    viewType: ViewType
) {
    val post: Post? = viewModel.post.collectAsState(initial = null).value

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PostItem(
                    modifier = Modifier.padding(top = 8.dp),
                    post = post,
                    postItemViewType = PostItemViewType.StandaloneView,
                    getUnicodeForEmojiId = { getEmojiForEmojiId(emojiId = it) }
                )
            }

            item {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(id = R.string.standalone_screen_replies_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            items(post?.answerPostings.orEmpty()) { answerPost ->
                AnswerPostItem(
                    modifier = Modifier.fillMaxWidth(),
                    answerPost = answerPost,
                    getUnicodeForEmojiId = { getEmojiForEmojiId(emojiId = it) }
                )
            }
        }
    }
}