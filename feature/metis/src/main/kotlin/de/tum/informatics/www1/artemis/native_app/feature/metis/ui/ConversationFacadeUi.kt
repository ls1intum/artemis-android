package de.tum.informatics.www1.artemis.native_app.feature.metis.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.feature.metis.R
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
    initialConfiguration: ConversationConfiguration = NothingOpened
) {
    val codeOfConductViewModel: CodeOfConductViewModel = koinViewModel { parametersOf(courseId) }

    ConversationFacadeUi(
        modifier = modifier,
        codeOfConductViewModel = codeOfConductViewModel,
        codeOfConductAcceptedContent = {
            SinglePageConversationBody(
                modifier = Modifier.fillMaxSize(),
                courseId = courseId,
                initialConfiguration = initialConfiguration
            )
        }
    )
}

/**
 * Wrapper that either displays the code of conduct if it has not yet been accepted, or the [codeOfConductAcceptedContent] otherwise.
 */
@Composable
internal fun ConversationFacadeUi(
    modifier: Modifier,
    codeOfConductViewModel: CodeOfConductViewModel,
    codeOfConductAcceptedContent: @Composable () -> Unit
) {
    val isCodeOfConductAcceptedDataState by codeOfConductViewModel.isCodeOfConductAccepted.collectAsState()
    val codeOfConductDataState by codeOfConductViewModel.codeOfConduct.collectAsState()

    CodeOfConductDataStateUi(
        modifier = modifier,
        dataState = isCodeOfConductAcceptedDataState join codeOfConductDataState,
        onClickRetry = codeOfConductViewModel::requestReload
    ) { (isCodeOfConductAccepted, codeOfConduct) ->
        when {
            codeOfConduct.isBlank() -> {
                NoCodeOfConductUi(modifier = Modifier.fillMaxSize())
            }

            isCodeOfConductAccepted -> {
                codeOfConductAcceptedContent()
            }

            else -> {
                val responsibleUsersDataState by codeOfConductViewModel.responsibleUsers.collectAsState()

                CodeOfConductDataStateUi(
                    modifier = Modifier.fillMaxSize(),
                    dataState = responsibleUsersDataState,
                    onClickRetry = codeOfConductViewModel::requestReload
                ) { responsibleUsers ->
                    AcceptCodeOfConductUi(
                        modifier = Modifier.fillMaxSize(),
                        codeOfConductText = codeOfConduct,
                        responsibleUsers = responsibleUsers,
                        onRequestAccept = codeOfConductViewModel::acceptCodeOfConduct
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> CodeOfConductDataStateUi(
    modifier: Modifier,
    dataState: DataState<T>,
    onClickRetry: () -> Unit,
    successUi: @Composable BoxScope.(T) -> Unit
) {
    BasicDataStateUi(
        modifier = modifier,
        dataState = dataState,
        loadingText = stringResource(id = R.string.code_of_conduct_failure),
        failureText = stringResource(id = R.string.code_of_conduct_failure),
        retryButtonText = stringResource(id = R.string.code_of_conduct_try_again),
        onClickRetry = onClickRetry,
        successUi = successUi
    )
}
