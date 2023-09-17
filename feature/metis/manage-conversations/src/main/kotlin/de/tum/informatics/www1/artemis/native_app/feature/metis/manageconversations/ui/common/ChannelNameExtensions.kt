package de.tum.informatics.www1.artemis.native_app.feature.metis.manageconversations.ui.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val channelNamePattern = "^[a-z0-9-]{1}[a-z0-9-]{0,20}\$".toRegex()

internal fun Flow<String>.mapIsChannelNameIllegal(): Flow<Boolean> =
    map { it.isNotEmpty() && !channelNamePattern.matches(it) }

internal fun Flow<String>.mapIsDescriptionOrTopicIllegal(): Flow<Boolean> =
    map { it.length > 250 }