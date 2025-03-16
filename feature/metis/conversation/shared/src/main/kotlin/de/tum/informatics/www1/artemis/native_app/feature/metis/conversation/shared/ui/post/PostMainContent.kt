package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.post

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tum.informatics.www1.artemis.native_app.core.ui.Spacings
import de.tum.informatics.www1.artemis.native_app.core.ui.date.DateFormats
import de.tum.informatics.www1.artemis.native_app.core.ui.date.format
import de.tum.informatics.www1.artemis.native_app.core.ui.date.getRelativeTime
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.core.ui.material.colors.PostColors
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.CreatePostStatus
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.service.model.LinkPreview
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IAnswerPost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IBasePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.IStandalonePost
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.UserRoleBadge
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.profile_picture.ProfilePictureWithDialog
import io.github.fornewid.placeholder.material3.placeholder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.time.Duration

private const val PLACEHOLDER_POST_CONTENT = "WWWWWWW"

@Composable
fun PostItemMainContent(
    modifier: Modifier = Modifier,
    post: IBasePost?,
    isExpanded: Boolean = true,
    isPlaceholder: Boolean = false,
    isDeleting: Boolean = false,
    isRoleBadgeVisible: Boolean = true,
    isAuthor: Boolean = false,
    postStatus: CreatePostStatus = CreatePostStatus.FINISHED,
    chatListItem: ChatListItem.PostItem? = null, // TODO: ADD support for eg. saved posts (https://github.com/ls1intum/artemis-android/issues/459)
    displayHeader: Boolean = true,
    linkPreviews: List<LinkPreview> = emptyList(),
    onRemoveLinkPreview: (LinkPreview) -> Unit = {},
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUndoDelete: () -> Unit = {},
    leadingContent: @Composable ColumnScope.() -> Unit = {},
    trailingContent: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
    ) {
        leadingContent()

        PostHeadline(
            modifier = Modifier.fillMaxWidth(),
            postStatus = postStatus,
            authorRole = post?.authorRole,
            authorName = post?.authorName,
            authorId = post?.authorId ?: -1,
            authorImageUrl = post?.authorImageUrl,
            creationDate = post?.creationDate,
            expanded = isExpanded,
            isAnswerPost = post is IAnswerPost,
            isRoleBadgeVisible = isRoleBadgeVisible,
            displayHeader = displayHeader,
            isDeleting = isDeleting
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isDeleting) {
                    UndoDeleteHeader(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onUndoDelete
                    )
                    return@PostHeadline
                }

                if (post?.content?.isNotEmpty() == true) {
                    (post as? IStandalonePost)?.takeIf { it.title?.isNotBlank() == true }
                        ?.let { standalonePost ->
                            standalonePost.title?.let {
                                Text(
                                    text = it,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PostColors.announcementTitle
                                )
                            }
                        }

                    MarkdownText(
                        markdown = remember(post.content, isPlaceholder) {
                            if (isPlaceholder) {
                                PLACEHOLDER_POST_CONTENT
                            } else post.content.orEmpty()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .placeholder(visible = isPlaceholder),
                        style = MaterialTheme.typography.bodyMedium,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        color = if (post.serverPostId == null) PostColors.unsentMessageText else Color.Unspecified
                    )
                }

                val instant = post?.updatedDate
                if (instant != null) {
                    Spacer(modifier = Modifier.height(Spacings.Post.innerSpacing))

                    val updateTime = instant.format(DateFormats.EditTimestamp.format)
                    Text(
                        text = stringResource(id = R.string.post_edited_hint, updateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = PostColors.editedHintText
                    )
                }

                if (linkPreviews.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacings.Post.innerSpacing))

                    LinkPreviewColumn(
                        modifier = Modifier.fillMaxWidth(),
                        linkPreviews = linkPreviews,
                        isAuthor = isAuthor,
                        onRemoveLinkPreview = onRemoveLinkPreview
                    )
                }

                if (chatListItem is ChatListItem.PostItem.ForwardedMessage) {
                    Spacer(modifier = Modifier.height(Spacings.Post.innerSpacing))

                    ForwardedMessageColumn(
                        modifier = Modifier.fillMaxWidth(),
                        chatListItem = chatListItem
                    )
                }

                trailingContent()
            }
        }
    }
}

@Composable
private fun PostHeadline(
    modifier: Modifier,
    authorRole: UserRole?,
    authorName: String?,
    authorId: Long,
    authorImageUrl: String?,
    creationDate: Instant?,
    postStatus: CreatePostStatus,
    isRoleBadgeVisible: Boolean,
    expanded: Boolean = false,
    isAnswerPost: Boolean,
    displayHeader: Boolean = true,
    isDeleting: Boolean,
    content: @Composable () -> Unit
) {
    val doDisplayHeader = displayHeader || postStatus == CreatePostStatus.FAILED

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacings.Post.innerSpacing)
        ) {
            if (!doDisplayHeader) {
                return@Row
            }

            HeadlineProfilePicture(
                userId = authorId,
                userName = authorName.orEmpty(),
                imageUrl = authorImageUrl,
                userRole = authorRole,
                isGrayscale = isDeleting
            )

            if (postStatus == CreatePostStatus.FAILED) {
                Text(
                    text = stringResource(id = R.string.post_sending_failed),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            HeadlineAuthorInfo(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                authorName = authorName,
                authorRole = authorRole,
                creationDate = creationDate,
                isRoleBadgeVisible = isRoleBadgeVisible,
                expanded = expanded,
                isAnswerPost = isAnswerPost,
                isGrayscale = isDeleting
            )
        }

        content()
    }
}


@Composable
private fun HeadlineAuthorInfo(
    modifier: Modifier,
    authorName: String?,
    authorRole: UserRole?,
    creationDate: Instant?,
    expanded: Boolean,
    isAnswerPost: Boolean,
    isGrayscale: Boolean,
    isRoleBadgeVisible: Boolean
) {
    Column(modifier = modifier) {
        if (isRoleBadgeVisible) {
            AuthorRoleAndTimeRow(
                expanded = expanded,
                authorRole = authorRole,
                creationDate = creationDate,
                isAnswerPost = isAnswerPost,
                isGrayscale = isGrayscale
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                modifier = Modifier,
                text = remember(authorName) { authorName ?: "Placeholder" },
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (!isRoleBadgeVisible) {
                Text("-")

                CreationDateContent(
                    modifier = Modifier,
                    creationDate = creationDate,
                    expanded = false,
                    showDateOnly = true,
                    isAnswerPost = false
                )
            }
        }
    }
}


@Composable
private fun AuthorRoleAndTimeRow(
    expanded: Boolean,
    authorRole: UserRole?,
    creationDate: Instant?,
    isAnswerPost: Boolean,
    isGrayscale: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /*
            * remember is needed here to prevent the value from being reset after an update
            * (the author role is not sent when updating a post)
            */
            val initialAuthorRole by remember { mutableStateOf(authorRole) }
            UserRoleBadge(
                modifier = Modifier.applyGrayscale(isGrayscale),
                userRole = initialAuthorRole
            )

            Spacer(modifier = Modifier.weight(1f))

            CreationDateContent(
                modifier = Modifier,
                creationDate = creationDate,
                expanded = expanded,
                isAnswerPost = isAnswerPost
            )
        }
    }
}

@Composable
private fun CreationDateContent(
    modifier: Modifier,
    creationDate: Instant?,
    expanded: Boolean,
    isAnswerPost: Boolean,
    showDateOnly: Boolean = false,
) {
    val relativeTimeTo = remember(creationDate) {
        creationDate ?: Clock.System.now()
    }

    val relativeTime = if (expanded || isAnswerPost) {
        getRelativeTime(to = relativeTimeTo, showDateAndTime = true)
    } else if (showDateOnly) {
        getRelativeTime(to = relativeTimeTo, showDate = true)
    } else {
        getRelativeTime(to = relativeTimeTo, showDate = false)
    }

    Text(
        modifier = modifier,
        text = relativeTime.toString(),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun HeadlineProfilePicture(
    userId: Long,
    userName: String,
    imageUrl: String?,
    userRole: UserRole?,
    displayImage: Boolean = true,
    isGrayscale: Boolean = false
) {
    val fontScale = LocalDensity.current.fontScale
    val scaledSizeDp = Spacings.Post.postHeadlineHeight * fontScale

    Box(
        modifier = Modifier
            .sizeIn(minWidth = scaledSizeDp, minHeight = scaledSizeDp)
            .applyGrayscale(isGrayscale)
    ) {
        if (!displayImage) {
            return
        }

        ProfilePictureWithDialog(
            modifier = Modifier.sizeIn(minWidth = scaledSizeDp, minHeight = scaledSizeDp),
            userId = userId,
            userName = userName,
            userRole = userRole,
            imageUrl = imageUrl,
        )
    }
}


@Composable
fun UndoDeleteHeader(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    delay: Duration = Duration.ofSeconds(6)
) {
    var remainingTime by remember { mutableLongStateOf(delay.seconds) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = delay.toMillis().toInt(),
                    easing = LinearEasing
                )
            )
        }
        while (remainingTime > 0) {
            delay(Duration.ofSeconds(1).toMillis())
            remainingTime--
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = pluralStringResource(
                id = R.plurals.post_is_being_deleted,
                count = remainingTime.toInt(),
                remainingTime.toInt()
            ),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.value)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(4.dp),
                text = stringResource(id = R.string.post_undo_delete),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}


fun Modifier.applyGrayscale(isGrayscale: Boolean): Modifier {
    return if (isGrayscale) {
        this
            .drawWithCache {
                onDrawWithContent {
                    drawContent()
                    drawRect(Color.Black, blendMode = BlendMode.Saturation)
                }
            }
    } else {
        this
    }
}