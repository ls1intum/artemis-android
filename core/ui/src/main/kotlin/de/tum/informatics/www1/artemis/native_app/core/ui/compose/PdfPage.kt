package de.tum.informatics.www1.artemis.native_app.core.ui.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile

/** A reading destination in the activity window, independent of the originating pane or sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPage(
    modifier: Modifier,
    pdfFile: PdfFile,
    title: String,
    progressKey: String?,
    initialPage: Int?,
    onBack: () -> Unit,
    onReloadSource: (() -> Unit)? = null,
    description: String? = null,
) {
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = modifier.testTag("media_pdf_page"),
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { NavigationBackButton(onNavigateBack = onBack) },
            )
        },
    ) { padding ->
        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize().padding(padding)) {
            if (!description.isNullOrBlank()) MediaDescription(description)
            ArtemisPdfView(
                Modifier.weight(1f).fillMaxSize(),
                pdfFile,
                onBack,
                progressKey,
                initialPage,
                showFilename = false,
                onReloadSource = onReloadSource,
            )
        }
    }
}
