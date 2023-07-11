package de.tum.informatics.www1.artemis.native_app.core.test.test_setup

val DefaultTimeoutMillis: Long get() = System.getenv("DEFAULT_TIMEOUT")?.toLong() ?: 10000L