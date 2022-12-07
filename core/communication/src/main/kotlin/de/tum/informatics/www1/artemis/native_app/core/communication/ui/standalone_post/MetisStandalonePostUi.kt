package de.tum.informatics.www1.artemis.native_app.core.communication.ui.standalone_post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisModificationFailure
import de.tum.informatics.www1.artemis.native_app.core.communication.MetisService
import de.tum.informatics.www1.artemis.native_app.core.communication.R
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.AnswerPostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.MetisModificationFailureDialog
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.getEmojiForEmojiId
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post
import org.koin.androidx.compose.get

@Composable
internal fun MetisStandalonePostUi(
    modifier: Modifier,
    viewModel: MetisStandalonePostViewModel,
    viewType: ViewType
) {
    val post: Post? = viewModel.post.collectAsState(initial = null).value

    var metisFailure: MetisModificationFailure? by remember { mutableStateOf(null) }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                val asAffectedPost = post?.let {
                    MetisService.AffectedPost.Standalone(
                        postId = it.serverPostId
                    )
                }

                PostItem(
                    modifier = Modifier.padding(top = 8.dp),
                    post = post,
                    postItemViewType = PostItemViewType.StandaloneView,
                    getUnicodeForEmojiId = { getEmojiForEmojiId(emojiId = it) },
                    onReactWithEmoji = { emojiId ->
                        viewModel.createReaction(
                            emojiId = emojiId,
                            post = asAffectedPost ?: return@PostItem,
                            response = { metisFailure = it }
                        )
                    },
                    onClickOnPresentReaction = { emojiId ->
                        viewModel.onClickReaction(
                            emojiId = emojiId,
                            post = asAffectedPost ?: return@PostItem,
                            presentReactions = post.reactions,
                            response = { metisFailure = it }
                        )
                    }
                )
            }

            item {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(id = R.string.standalone_screen_replies_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            items(post?.orderedAnswerPostings.orEmpty(), key = { it.postId }) { answerPost ->
                AnswerPostItem(
                    modifier = Modifier.fillMaxWidth(),
                    answerPost = answerPost,
                    getUnicodeForEmojiId = { getEmojiForEmojiId(emojiId = it) },
                    onReactWithEmoji = { emojiId ->
                        viewModel.createReactionForAnswer(
                            emojiId = emojiId,
                            clientSidePostId = answerPost.postId,
                            onResponse = { metisFailure = it }
                        )
                    },
                    onClickOnPresentReaction = { emojiId ->
                        viewModel.onClickReactionOfAnswer(
                            emojiId = emojiId,
                            clientSidePostId = answerPost.postId,
                            presentReactions = answerPost.reactions,
                            onResponse = { metisFailure = it }
                        )
                    }
                )
            }
        }
    }

    MetisModificationFailureDialog(metisModificationFailure = metisFailure) {
        metisFailure = null
    }
}