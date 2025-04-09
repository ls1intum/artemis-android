package de.tum.informatics.www1.artemis.native_app.feature.courseview

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.shared.ui.ChatListItem
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.UserRole
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ChannelChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.ConversationUser
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.GroupChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.content.dto.conversation.OneToOneChat
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.db.pojo.PostPojo
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object ScreenshotCommunicationData {

    val conversation = ChannelChat(
        id = 1L,
        name = "Designing a rocket engine - Q&A",
        unreadMessagesCount = 17,
        numberOfMembers = 64
    )

    val conversations = listOf(
        ChannelChat(
            id = 0L,
            name = "General Course Questions",
            unreadMessagesCount = 8,
        ),
        conversation,
        GroupChat(
            id = 2L,
            name = "Team Rocket Students"
        ),
        OneToOneChat(
            id = 3L,
            members = listOf(
                ConversationUser(
                    username = "u1",
                    firstName = "Ethan",
                    lastName = "Martin"
                )
            )
        ),
        OneToOneChat(
            id = 4L,
            members = listOf(
                ConversationUser(
                    username = "u2",
                    firstName = "Sophia",
                    lastName = "Davis"
                )
            ),
            unreadMessagesCount = 3,
        )
    )

    private val firstMessageTime = Clock.System.now() - 20.minutes

    val posts = listOf(
        ChatListItem.DateDivider(firstMessageTime.toLocalDateTime(TimeZone.currentSystemDefault()).date),
        generateMessage(
            name = "Sam",
            text = "Hey, folks! What are the big advantages of solid chemical propellants in rockets?",
            time = firstMessageTime,
            id = "1",
            authorId = 0L
        ),
        generateMessage(
            name = "Mia",
            text = "Hey, Sam! Solid propellants are known for their simplicity and reliability. They're easy to handle.",
            time = firstMessageTime + 3.minutes,
            id = "2",
            authorId = 1L
        ),
        generateMessage(
            name = "Ethan",
            text = "That's right, Mia. They have a consistent burn rate and a good thrust-to-weight ratio, which makes them handy for various missions.",
            time = firstMessageTime + 12.minutes,
            id = "3",
            authorId = 2L
        ),
        generateMessage(
            name = "Sam",
            text = " Thanks, Mia and Ethan! So, they're like the dependable workhorses of rocket propellants, huh?",
            time = firstMessageTime + 15.minutes,
            id = "4",
            authorId = 0L
        ),
    ).reversed()

    private fun generateMessage(
        name: String,
        text: String,
        time: Instant,
        id: String,
        authorId: Long
    ): ChatListItem.PostItem {
        return ChatListItem.PostItem.IndexedItem.Post(
            PostPojo(
                clientPostId = id,
                serverPostId = 0L,
                title = null,
                content = text,
                authorName = name,
                authorRole = UserRole.USER,
                authorId = authorId,
                authorImageUrl = null,
                creationDate = time,
                updatedDate = null,
                resolved = false,
                isSaved = false,
                courseWideContext = null,
                tags = emptyList(),
                answers = emptyList(),
                reactions = emptyList(),
                displayPriority = null,
                hasForwardedMessages = false
            ),
            emptyList()
        )
    }

}