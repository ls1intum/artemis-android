package de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.ui

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.tum.informatics.www1.artemis.native_app.core.data.DataState
import de.tum.informatics.www1.artemis.native_app.core.data.join
import de.tum.informatics.www1.artemis.native_app.core.model.account.User
import de.tum.informatics.www1.artemis.native_app.core.ui.common.BasicDataStateUi
import de.tum.informatics.www1.artemis.native_app.core.ui.markdown.MarkdownText
import de.tum.informatics.www1.artemis.native_app.feature.metis.codeofconduct.R
import de.tum.informatics.www1.artemis.native_app.feature.metis.shared.ui.humanReadableName
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CodeOfConductUi(
    modifier: Modifier,
    courseId: Long
) {
    val codeOfConductViewModel: CodeOfConductViewModel = koinViewModel { parametersOf(courseId) }

    val codeOfConductDataState by codeOfConductViewModel.codeOfConduct.collectAsState()
    val responsibleUsersDataState by codeOfConductViewModel.responsibleUsers.collectAsState()

    CodeOfConductDataStateUi(
        modifier = modifier,
        dataState = codeOfConductDataState join responsibleUsersDataState,
        onClickRetry = codeOfConductViewModel::requestReload
    ) { (codeOfConduct, responsibleUsers) ->
        CodeOfConductUi(
            modifier = Modifier.fillMaxWidth(),
            codeOfConductText = codeOfConduct,
            responsibleUsers = responsibleUsers
        )
    }
}

@Composable
fun CodeOfConductUi(
    modifier: Modifier,
    codeOfConductText: String,
    responsibleUsers: List<User>
) {
    // Simply display responsible users by appending corresponding markdown
    val codeOfConductTextWithResponsibleUsers: String = remember(responsibleUsers) {
        val responsibleUsersText =
            responsibleUsers.joinToString(separator = "\n") { responsibleUser ->
                "- ${responsibleUser.humanReadableName} (${responsibleUser.email})"
            }

        codeOfConductText + "\n" + responsibleUsersText
    }

    MarkdownText(
        modifier = modifier,
        markdown = codeOfConductTextWithResponsibleUsers
    )
}

@Composable
internal fun <T> CodeOfConductDataStateUi(
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
