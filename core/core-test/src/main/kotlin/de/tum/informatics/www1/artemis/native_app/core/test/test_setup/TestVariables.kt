package de.tum.informatics.www1.artemis.native_app.core.test.test_setup

val DefaultTimeoutMillis: Long get() = System.getenv("defaultTimeout")?.toLong() ?: 1000L