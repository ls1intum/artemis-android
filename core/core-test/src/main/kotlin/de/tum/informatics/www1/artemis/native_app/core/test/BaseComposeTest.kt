package de.tum.informatics.www1.artemis.native_app.core.test

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.koin.test.KoinTest

abstract class BaseComposeTest : KoinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val testDispatcher = UnconfinedTestDispatcher()

    protected val context: Context get() = InstrumentationRegistry.getInstrumentation().context
}