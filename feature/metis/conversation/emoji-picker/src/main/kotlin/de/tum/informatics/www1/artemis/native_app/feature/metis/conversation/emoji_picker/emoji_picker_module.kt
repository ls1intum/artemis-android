package de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker

import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.EmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.RecentEmojiService
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl.EmojiServiceImpl
import de.tum.informatics.www1.artemis.native_app.feature.metis.conversation.emoji_picker.service.impl.RecentEmojiServiceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val emojiPickerModule = module {
    single<RecentEmojiService> { RecentEmojiServiceImpl(androidContext()) }
    single<EmojiService> { EmojiServiceImpl(androidContext(), get()) }
}