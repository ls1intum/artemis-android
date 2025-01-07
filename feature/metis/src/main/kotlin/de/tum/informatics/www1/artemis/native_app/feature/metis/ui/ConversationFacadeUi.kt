package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.feature.metis.ConversationConfiguration
import de.tum.informatics.www1.artemis.native_app.feature.metis.NothingOpened
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui.CodeOfConductFacadeUi
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Displays the conversation ui. If the code of conduct has not yet been accepted, displays a code
 * of conduct message before anything else can be displayed to the user.
 */
@Composable
fun ConversationFacadeUi(
    modifier: Modifier,
    courseId: Long,
    initialConfiguration: ConversationConfiguration = NothingOpened,
    onRefresh: () -> Unit
) {
    CodeOfConductFacadeUi(
        modifier = modifier,
        courseId = courseId,
        codeOfConductAcceptedContent = {
            SinglePageConversationBody(
                modifier = Modifier.fillMaxSize(),
                viewModel = koinViewModel { parametersOf(courseId) },
                courseId = courseId,
                initialConfiguration = initialConfiguration,
                onRefresh = onRefresh
            )
        }
    )
}
