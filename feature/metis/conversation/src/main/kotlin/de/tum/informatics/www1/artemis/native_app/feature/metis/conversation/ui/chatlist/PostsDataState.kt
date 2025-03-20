package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.ui.chatlist

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem

/**
 * Abstract layer over the loaded posts state, so we can use both a Pager and a simple list to display posts.
 * Mimics the API of [LazyPagingItems]
 */
sealed interface PostsDataState {
    val itemCount: Int

    fun peek(index: Int): ChatListItem?

    data object Empty : PostsDataState {
        override val itemCount: Int = 0

        override fun peek(index: Int): ChatListItem? = null
    }

    data object Loading : PostsDataState, AppendState {
        override val itemCount: Int = 0

        override fun peek(index: Int): ChatListItem? = null
    }

    data class Error(val retry: () -> Unit) : PostsDataState, AppendState {
        override val itemCount: Int = 0

        override fun peek(index: Int): ChatListItem? = null
    }

    data object NotLoading : AppendState

    sealed interface Loaded : PostsDataState {
        val appendState: AppendState

        operator fun get(index: Int): ChatListItem?

        fun getItemKey(index: Int): Any

        data class WithLazyPagingItems(
            val posts: LazyPagingItems<ChatListItem>,
            override val appendState: AppendState
        ) : Loaded {
            override val itemCount: Int
                get() = posts.itemCount

            override fun getItemKey(index: Int): Any = posts[index]?.getItemKey() ?: index

            override fun get(index: Int): ChatListItem? = posts[index]

            override fun peek(index: Int): ChatListItem? = posts.peek(index)
        }

        data class WithList(
            val posts: List<ChatListItem>,
            override val appendState: AppendState
        ) : Loaded {
            override val itemCount: Int = posts.size

            override fun getItemKey(index: Int): Any = posts[index].getItemKey()

            override fun get(index: Int): ChatListItem = posts[index]

            override fun peek(index: Int): ChatListItem? = posts.getOrNull(index)
        }
    }

    sealed interface AppendState
}

fun LazyPagingItems<ChatListItem>.asPostsDataState(): PostsDataState = when {
    itemCount == 0 -> {
        if (loadState.isIdle) {
            PostsDataState.Empty
        } else {
            PostsDataState.Loading
        }
    }
    loadState.refresh is LoadState.Loading -> PostsDataState.Loading
    loadState.refresh is LoadState.Error -> PostsDataState.Error(::retry)
    else -> PostsDataState.Loaded.WithLazyPagingItems(
        posts = this,
        appendState = when (loadState.append) {
            LoadState.Loading -> PostsDataState.Loading
            is LoadState.Error -> PostsDataState.Error(::retry)
            is LoadState.NotLoading -> PostsDataState.NotLoading
        }
    )
}
