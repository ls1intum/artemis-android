package de.tum.informatics.www1.artemis.native_app.core.test.test_setup

import java.util.UUID

fun generateId(): String = UUID.randomUUID().toString().replace('-', '1')