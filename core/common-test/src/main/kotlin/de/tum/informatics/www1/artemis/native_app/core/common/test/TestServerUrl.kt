package de.tum.informatics.www1.artemis.native_app.core.common.test

val testServerUrl: String
    get() = System.getenv("SERVER_URL") ?: "http://localhost:8080"