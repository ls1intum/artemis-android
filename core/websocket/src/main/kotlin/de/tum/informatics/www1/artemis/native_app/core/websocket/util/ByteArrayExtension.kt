package de.tum.informatics.www1.artemis.native_app.core.websocket.util

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.ByteStringBuilder

fun ByteArray.toByteString(): ByteString {
    val byteStringBuilder = ByteStringBuilder()
    byteStringBuilder.append(this)
    return byteStringBuilder.toByteString()
}