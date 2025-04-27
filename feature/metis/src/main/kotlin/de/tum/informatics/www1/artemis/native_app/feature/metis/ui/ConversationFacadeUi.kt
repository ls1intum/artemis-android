package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.ui.common.course.CourseSearchConfiguration
import de.tum.informatics.www1.artemis.native_app.core.ui.common.top_app_bar.CollapsingContentState
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
    scaffold: @Composable (searchConfiguration: CourseSearchConfiguration, content: @Composable () -> Unit) -> Unit,
    collapsingContentState: CollapsingContentState,
    initialConfiguration: ConversationConfiguration = NothingOpened,
    title: String?
) {
    var showCodeOfConduct by remember { mutableStateOf(true) }

    if (showCodeOfConduct) {
        scaffold(CourseSearchConfiguration.DisabledSearch) {
            CodeOfConductFacadeUi(
                modifier = modifier,
                courseId = courseId,
                onCodeOfConductAccepted = {
                    showCodeOfConduct = false
                }
            )
        }
    } else {
        SinglePageConversationBody(
            modifier = Modifier.fillMaxSize(),
            viewModel = koinViewModel { parametersOf(courseId) },
            courseId = courseId,
            scaffold = scaffold,
            collapsingContentState = collapsingContentState,
            initialConfiguration = initialConfiguration,
            title = title
        )
    }
}
