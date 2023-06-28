package de.tum.informatics.www1.artemis.native_app.core.test

val testServerUrl: String
    get() = System.getenv("serverUrl") ?: "https://localhost"