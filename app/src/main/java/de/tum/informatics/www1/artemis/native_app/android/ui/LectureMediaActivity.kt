package de.tum.informatics.www1.artemis.native_app.android.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import de.tum.informatics.www1.artemis.native_app.android.ui.theme.AppTheme
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.ui.*
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.PdfPage
import de.tum.informatics.www1.artemis.native_app.core.ui.media.*
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.LectureVideoPlayer
import java.util.UUID
import org.koin.compose.koinInject

/**
 * Private full-window destination. Authentication is read from the live account, never the intent.
 */
class LectureMediaActivity : ComponentActivity() {
    companion object {
        private const val SOURCE = "source"
        private const val TITLE = "title"
        private const val IDENTITY = "identity"
        private const val SCOPE = "scope"
        private const val KIND = "kind"
        private const val PAGE = "page"
        private const val EPOCH = "epoch"
        internal const val DESCRIPTION = "description"
        private const val RELATED_SOURCE = "related_source"
        private const val RELATED_TITLE = "related_title"
        private const val RELATED_IDENTITY = "related_identity"

        internal fun writeMetadata(intent: Intent, request: MediaPageRequest) {
            intent.putExtra(TITLE, request.title)
            intent.putExtra(SCOPE, request.scope)
            intent.putExtra(DESCRIPTION, request.description)
            intent.putExtra(EPOCH, processEpoch)
            request.relatedPdf?.let {
                intent.putExtra(RELATED_SOURCE, it.url)
                intent.putExtra(RELATED_TITLE, it.title)
                intent.putExtra(RELATED_IDENTITY, it.identity)
            }
        }

        internal fun relatedIntent(
            context: Context,
            source: Intent,
            request: MediaPageRequest,
        ): Intent = intent(context, request).putExtra(EPOCH, source.getStringExtra(EPOCH))

        internal fun relatedPdf(intent: Intent): MediaPageRequest? =
            intent.getStringExtra(RELATED_SOURCE)?.let {
                MediaPageRequest(
                    it,
                    intent.getStringExtra(RELATED_TITLE).orEmpty(),
                    intent.getStringExtra(RELATED_IDENTITY).orEmpty(),
                    intent.getStringExtra(SCOPE).orEmpty(),
                    MediaPageKind.PDF,
                )
            }

        private val processEpoch = UUID.randomUUID().toString()

        internal fun intent(context: Context, request: MediaPageRequest) =
            Intent(context, LectureMediaActivity::class.java).apply {
                putExtra(SOURCE, request.url)
                putExtra(TITLE, request.title)
                putExtra(IDENTITY, request.identity)
                putExtra(SCOPE, request.scope)
                putExtra(KIND, request.kind.name)
                putExtra(EPOCH, processEpoch)
                request.initialPage?.let { putExtra(PAGE, it) }
                writeMetadata(this, request)
            }
    }

    private val openedPdf by lazy {
        androidx.lifecycle.ViewModelProvider(this)[OpenedPdfPage::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val url = intent.getStringExtra(SOURCE).orEmpty()
        val kind = runCatching {
            MediaPageKind.valueOf(intent.getStringExtra(KIND).orEmpty())
        }
            .getOrNull()
        if (mediaHttpUri(url) == null || kind == null) {
            finish()
            return
        }
        val title = intent.getStringExtra(TITLE).orEmpty()
        val identity = intent.getStringExtra(IDENTITY).orEmpty()
        val expectedScope = intent.getStringExtra(SCOPE)
        // A killed process must re-enter through its origin and fresh metadata, not reuse a mapped
        // page with a potentially replaced PDF. Configuration changes in this process keep the
        // page.
        val sourceStillCurrent =
            intent.getStringExtra(EPOCH) == processEpoch &&
                (savedInstanceState == null || kind != MediaPageKind.PDF || openedPdf.hasSnapshot)
        val related = relatedPdf(intent)
        val openAttachment: (() -> Unit)? = related?.let { request ->
            { startActivity(intent(this, request)) }
        }
        val description = intent.getStringExtra(DESCRIPTION)
        setContent {
            AppTheme {
                val provider = koinInject<ArtemisContextProvider>()
                val session by provider.collectArtemisContextAsState()
                val browser = remember {
                    object : LinkOpener {
                        override fun openLink(url: String) {
                            CustomTabsIntent.Builder()
                                .build()
                                .launchUrl(this@LectureMediaActivity, url.toUri())
                        }
                    }
                }
                CompositionLocalProvider(
                    LocalArtemisContextProvider provides provider,
                    LocalLinkOpener provides browser,
                ) {
                    if (expectedScope != mediaPageScope(session)) {
                        LaunchedEffect(Unit) { finish() }
                    } else if (!sourceStillCurrent) ReopenSourcePage(::finish)
                    else
                        when (kind) {
                            MediaPageKind.PDF -> {
                                val token =
                                    if (sameMediaOrigin(session.serverUrl, url))
                                        session.authTokenOrEmptyString
                                    else ""
                                val progress =
                                    mediaProgressKey(
                                        (session as? ArtemisContext.LoggedIn)?.loginName.orEmpty(),
                                        session.serverUrl,
                                        identity,
                                        url,
                                    )
                                val file = openedPdf.getOrCreate {
                                    PdfFile(
                                        url,
                                        token,
                                        mediaHttpUri(url)?.path?.substringAfterLast('/').orEmpty(),
                                        session.serverUrl,
                                        progress,
                                    )
                                }
                                PdfPage(
                                    Modifier.fillMaxSize(),
                                    file,
                                    title,
                                    progress,
                                    intent.getIntExtra(PAGE, -1).takeIf { it > 0 },
                                    ::finish,
                                    description = description,
                                )
                            }
                            MediaPageKind.VIDEO ->
                                LectureVideoPlayer(
                                    url,
                                    identity,
                                    ::finish,
                                    title,
                                    description = description,
                                    onOpenAttachment = openAttachment,
                                )
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReopenSourcePage(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { NavigationBackButton(onNavigateBack = onBack) },
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(CoreR.string.lecture_media_missing))
            Text(stringResource(CoreR.string.lecture_media_reopen_source))
            TextButton(onClick = onBack) { Text(stringResource(CoreR.string.lecture_media_close)) }
        }
    }
}
