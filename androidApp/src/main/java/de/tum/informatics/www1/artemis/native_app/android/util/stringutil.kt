package de.tum.informatics.www1.artemis.native_app.android.util

fun String.withoutLastChar(): String = if (isEmpty()) "" else substring(0, length - 1)