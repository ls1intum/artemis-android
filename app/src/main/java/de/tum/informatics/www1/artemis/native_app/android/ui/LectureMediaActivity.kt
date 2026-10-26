package de.tum.informatics.www1.artemis.native_app.android.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import de.tum.informatics.www1.artemis.native_app.android.ui.theme.AppTheme
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.ui.*
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.NavigationBackButton
import de.tum.informatics.www1.artemis.native_app.core.ui.compose.PdfPage
import de.tum.informatics.www1.artemis.native_app.core.ui.media.*
import de.tum.informatics.www1.artemis.native_app.core.ui.remote_resources.pdf.PdfFile
import de.tum.informatics.www1.artemis.native_app.feature.lectureview.LectureVideoPlayer
import org.koin.compose.koinInject
import java.util.UUID
import de.tum.informatics.www1.artemis.native_app.core.ui.R as CoreR

/** Private full-window destination. Authentication is read from the live account, never the intent. */
class LectureMediaActivity: ComponentActivity() {
    companion object {
        private const val SOURCE="source"
        private const val TITLE="title"
        private const val IDENTITY="identity"
        private const val SCOPE="scope"
        private const val KIND="kind"
        private const val PAGE="page"
        private const val EPOCH="epoch"
        private val processEpoch=UUID.randomUUID().toString()
        internal fun intent(context:Context,request:MediaPageRequest)=Intent(context,LectureMediaActivity::class.java).apply {
            putExtra(SOURCE,request.url);putExtra(TITLE,request.title);putExtra(IDENTITY,request.identity)
            putExtra(SCOPE,request.scope);putExtra(KIND,request.kind.name);putExtra(EPOCH,processEpoch)
            request.initialPage?.let{putExtra(PAGE,it)}
        }
    }
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val url=intent.getStringExtra(SOURCE).orEmpty()
        val kind=runCatching{MediaPageKind.valueOf(intent.getStringExtra(KIND).orEmpty())}.getOrNull()
        if(mediaHttpUri(url)==null||kind==null){finish();return}
        val title=intent.getStringExtra(TITLE).orEmpty()
        val identity=intent.getStringExtra(IDENTITY).orEmpty()
        val expectedScope=intent.getStringExtra(SCOPE)
        // A killed process must re-enter through its origin and fresh metadata, not reuse a mapped
        // page with a potentially replaced PDF. Configuration changes in this process keep the page.
        val sourceStillCurrent=intent.getStringExtra(EPOCH)==processEpoch
        setContent {
            AppTheme {
                val provider=koinInject<ArtemisContextProvider>()
                val session by provider.collectArtemisContextAsState()
                val browser=remember { object:LinkOpener {
                    override fun openLink(url:String) { CustomTabsIntent.Builder().build().launchUrl(this@LectureMediaActivity,url.toUri()) }
                } }
                CompositionLocalProvider(LocalArtemisContextProvider provides provider,LocalLinkOpener provides browser) {
                    if(expectedScope!=mediaPageScope(session)) {
                        LaunchedEffect(Unit){finish()}
                    } else if(!sourceStillCurrent) ReopenSourcePage(::finish)
                    else when(kind) {
                        MediaPageKind.PDF -> {
                            val token=if(sameMediaOrigin(session.serverUrl,url))session.authTokenOrEmptyString else ""
                            val progress=mediaProgressKey((session as? ArtemisContext.LoggedIn)?.loginName.orEmpty(),session.serverUrl,identity,url)
                            val file=remember(url,token,progress){PdfFile(url,token,title,session.serverUrl,progress)}
                            PdfPage(Modifier.fillMaxSize(),file,title,progress,intent.getIntExtra(PAGE,-1).takeIf{it>0},::finish)
                        }
                        MediaPageKind.VIDEO -> LectureVideoPlayer(url,identity,::finish,title)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReopenSourcePage(onBack:()->Unit) {
    Scaffold(topBar={TopAppBar(title={},navigationIcon={NavigationBackButton(onNavigateBack=onBack)})}) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp),verticalArrangement=Arrangement.Center) {
            Text(stringResource(CoreR.string.lecture_media_missing))
            Text(stringResource(CoreR.string.lecture_media_reopen_source))
            TextButton(onClick=onBack){Text(stringResource(CoreR.string.lecture_media_close))}
        }
    }
}
