package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import de.tum.informatics.www1.artemis.native_app.core.data.join
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Wrapper that either displays the code of conduct if it has not yet been accepted, or the [onCodeOfConductAccepted] otherwise.
 */
@Composable
fun CodeOfConductFacadeUi(
    modifier: Modifier,
    courseId: Long,
    onCodeOfConductAccepted: () -> Unit
) {
    val codeOfConductViewModel: CodeOfConductViewModel = koinViewModel { parametersOf(courseId) }

    CodeOfConductFacadeUi(
        modifier = modifier,
        codeOfConductViewModel = codeOfConductViewModel,
        onCodeOfConductAccepted = onCodeOfConductAccepted
    )
}

@Composable
internal fun CodeOfConductFacadeUi(
    modifier: Modifier,
    codeOfConductViewModel: CodeOfConductViewModel,
    onCodeOfConductAccepted: () -> Unit
) {
    val isCodeOfConductAcceptedDataState by codeOfConductViewModel.isCodeOfConductAccepted.collectAsState()
    val codeOfConductDataState by codeOfConductViewModel.codeOfConduct.collectAsState()

    CodeOfConductDataStateUi(
        modifier = modifier,
        dataState = isCodeOfConductAcceptedDataState join codeOfConductDataState,
        onClickRetry = codeOfConductViewModel::requestReload
    ) { (isCodeOfConductAccepted, codeOfConduct) ->
        when {
            isCodeOfConductAccepted -> {
                onCodeOfConductAccepted()
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
