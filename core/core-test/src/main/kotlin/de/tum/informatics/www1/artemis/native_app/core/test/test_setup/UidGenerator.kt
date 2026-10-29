package de.tum.informatics.www1.artemis.native_app.core.test.test_setup

import java.util.UUID

fun generateId(): String = UUID.randomUUID().toString().replace('-', '1')

/**
 * A unique short name that fits what the server accepts.
 *
 * Course short names are limited to 24 characters and have to start with a letter, so the full
 * [generateId] is too long to use as one.
 */
fun generateShortName(prefix: String = "ae2e"): String =
    (prefix + generateId()).take(COURSE_SHORT_NAME_MAX_LENGTH)

/** Mirrors the server's own limit. */
private const val COURSE_SHORT_NAME_MAX_LENGTH = 24
