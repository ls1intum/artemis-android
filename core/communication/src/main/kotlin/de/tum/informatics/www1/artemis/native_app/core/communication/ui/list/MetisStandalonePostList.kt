package de.tum.informatics.www1.artemis.native_app.core.communication.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItem
import de.tum.informatics.www1.artemis.native_app.core.communication.ui.PostItemViewType
import de.tum.informatics.www1.artemis.native_app.core.datastore.model.metis.Post

@Composable
internal fun MetisStandalonePostList(
    modifier: Modifier,
    viewModel: MetisListViewModel,
    onClickReply: (clientPostId: String) -> Unit,
    onClickViewReplies: (clientPostId: String) -> Unit
) {
    val posts: LazyPagingItems<Post> = viewModel.postPagingData.collectAsLazyPagingItems()

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            items(posts) { post ->
                PostItem(
                    modifier = Modifier.fillMaxWidth(),
                    post = post,
                    postItemViewType = PostItemViewType.StandaloneListItem(
                        answerPosts = post?.answerPostings.orEmpty(),
                        onClickReply = {
                            if (post != null) {
                                onClickReply(post.clientPostId)
                            }
                        },
                        onClickViewReplies = {
                            if (post != null) {
                                onClickViewReplies(post.clientPostId)
                            }
                        }
                    )
                )
            }
        }
    }
}